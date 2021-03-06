/*
 * Copyright 2011 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbConnectionManager;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbDeviceManager;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.AdbException;
import org.bitbucket.mlopatkin.android.liblogcat.ddmlib.DdmlibUnsupportedException;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper;
import org.bitbucket.mlopatkin.android.logviewer.widgets.UiHelper.DoubleClickListener;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;

public class SelectDeviceDialog extends JDialog {

    public interface DialogResultReceiver {
        void onDialogResult(SelectDeviceDialog dialog, IDevice selectedDevice);
    }

    public static void showSelectDeviceDialog(MainFrame owner, DialogResultReceiver receiver) {
        assert !AdbConnectionManager.isFailed();
        if (!AdbConnectionManager.isReady()) {
            try {
                AdbConnectionManager.init();
            } catch (AdbException e) {
                logger.warn("Cannot start in ADB mode", e);
                owner.disableAdbCommandsAsync();
                ErrorDialogsHelper.showAdbNotFoundError(owner);
                return;
            } catch (DdmlibUnsupportedException e) {
                logger.error("Cannot work with DDMLIB supplied", e);
                owner.disableAdbCommandsAsync();
                ErrorDialogsHelper.showError(owner, e.getMessage());
                return;
            }
        }
        if (dialog == null) {
            dialog = new SelectDeviceDialog(owner);
        }
        dialog.receiver = receiver;
        dialog.deviceList.clearSelection();
        dialog.setVisible(true);
    }

    private static final Logger logger = Logger.getLogger(SelectDeviceDialog.class);

    private static SelectDeviceDialog dialog;

    private JList deviceList;
    private DialogResultReceiver receiver;

    private DeviceListModel devices = new DeviceListModel();

    private SelectDeviceDialog(Frame owner) {
        super(owner, true);
        setTitle("Select device");
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            deviceList = new JList();
            deviceList.setModel(devices);
            contentPanel.add(deviceList);

            UiHelper.addDoubleClickListener(deviceList, new DoubleClickListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onPositiveResult();
                }
            });
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onPositiveResult();
                    }
                });

                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onNegativeResult();
                    }
                });
                buttonPane.add(cancelButton);
            }
        }
        updater.start();
    }

    private void onPositiveResult() {
        assert receiver != null;
        IDevice selectedDevice = getSelectedDevice();
        if (selectedDevice == null || selectedDevice.isOnline()) {
            receiver.onDialogResult(this, getSelectedDevice());
            setVisible(false);
        } else {
            ErrorDialogsHelper.showError(this, "Can't connect to offline device");
        }

    }

    private void onNegativeResult() {
        assert receiver != null;
        receiver.onDialogResult(this, null);
        setVisible(false);
    }

    private IDevice getSelectedDevice() {
        int selected = deviceList.getSelectedIndex();
        if (selected >= 0) {
            return devices.getDevice(selected);
        } else {
            return null;
        }
    }

    private static class DeviceListModel extends AbstractListModel implements IDeviceChangeListener {

        private List<IDevice> devices;

        public DeviceListModel() {
            devices = new ArrayList<IDevice>();
            for (IDevice device : AdbDeviceManager.getAvailableDevices()) {
                devices.add(device);
            }
            AdbDeviceManager.addDeviceChangeListener(this);
        }

        @Override
        public int getSize() {
            return devices.size();
        }

        private String formatOfflineDevice(String deviceName) {
            return "<html><i> Offline (" + deviceName + ")</i></html>";
        }

        @Override
        public Object getElementAt(int index) {
            IDevice device = getDevice(index);
            String deviceName = AdbDeviceManager.getDeviceDisplayName(device);
            if (device.isOnline()) {
                return deviceName;
            } else {
                return formatOfflineDevice(deviceName);
            }
        }

        public IDevice getDevice(int index) {
            return devices.get(index);
        }

        private void addDevice(IDevice device) {
            logger.debug("device added " + device);
            devices.add(device);
            fireIntervalAdded(this, devices.size() - 1, devices.size() - 1);
        }

        private void removeDevice(IDevice device) {
            logger.debug("device removed " + device);
            int index = devices.indexOf(device);
            if (index >= 0) {
                devices.remove(index);
                fireIntervalRemoved(this, index, index);
            }
        }

        @Override
        public void deviceConnected(final IDevice device) {
            logger.debug("Device connected: " + device);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    addDevice(device);
                }
            });
        }

        @Override
        public void deviceDisconnected(final IDevice device) {
            logger.debug("Device disconnected: " + device);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    removeDevice(device);
                }
            });
        }

        private void updateState(IDevice device, int changeMask) {
            if ((changeMask & (IDevice.CHANGE_STATE | IDevice.CHANGE_BUILD_INFO)) != 0) {
                fireContentsChanged(DeviceListModel.this, 0, devices.size() - 1);
            }
        }

        @Override
        public void deviceChanged(final IDevice device, final int changeMask) {
            logger.debug("Device changed: " + device + " changeMask="
                    + Integer.toHexString(changeMask));
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateState(device, changeMask);
                }
            });
        }

    }

    private static final int UPDATE_DELAY = 500;
    private Timer updater = new Timer(UPDATE_DELAY, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isVisible()) {
                deviceList.repaint();
            }
        }
    });
}
