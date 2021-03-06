package org.bitbucket.mlopatkin.android.logviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Priority;
import org.bitbucket.mlopatkin.android.logviewer.FilteringModesPanel.ModeChangedListener;
import org.bitbucket.mlopatkin.android.logviewer.search.SearchStrategyFactory;

public abstract class FilterDialog extends JDialog {

    private final JPanel contentPanel = new JPanel();

    private JTextField tagTextField;
    private JTextField messageTextField;
    private JTextField pidTextField;

    private JComboBox logLevelList;

    private FilteringModesPanel modesPanel;
    private JComboBox colorsList;

    private ModeChangedListener modeListener = new ModeChangedListener() {

        @Override
        public void modeSelected(FilteringMode mode) {
            colorsList.setVisible(mode == FilteringMode.HIGHLIGHT);
            colorsList.revalidate();
            colorsList.repaint();
        }
    };

    /**
     * Create the dialog.
     */
    protected FilterDialog(Frame owner) {
        super(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initialize();
    }

    private void initialize() {
        setBounds(100, 100, 527, 390);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            tagTextField = new JTextField();
            tagTextField.setColumns(10);
        }

        JLabel lblNewLabel = new JLabel("Tags to filter");

        JLabel lblMessageTextTo = new JLabel("Message text to filter");

        messageTextField = new JTextField();
        messageTextField.setColumns(10);

        JLabel lblPidsToFilter = new JLabel("PIDs to filter");

        pidTextField = new JTextField();
        pidTextField.setColumns(10);

        JLabel lblLogLevel = new JLabel("Log level");

        logLevelList = new JComboBox(new PriorityComboBoxModel());

        JPanel modesWithDataPanel = new JPanel();

        colorsList = new JComboBox(new ColorsComboBoxModel());
        colorsList.setSelectedIndex(0);

        GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
        gl_contentPanel.setHorizontalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel.createSequentialGroup().addContainerGap().addGroup(
                                gl_contentPanel.createParallelGroup(Alignment.LEADING)
                                        .addComponent(tagTextField, GroupLayout.DEFAULT_SIZE, 477,
                                                Short.MAX_VALUE).addComponent(lblNewLabel)
                                        .addComponent(lblMessageTextTo).addComponent(
                                                messageTextField, GroupLayout.DEFAULT_SIZE, 477,
                                                Short.MAX_VALUE).addComponent(lblPidsToFilter)
                                        .addComponent(pidTextField, GroupLayout.DEFAULT_SIZE, 477,
                                                Short.MAX_VALUE).addComponent(lblLogLevel)
                                        .addComponent(logLevelList, 0, 477, Short.MAX_VALUE)
                                        .addGroup(
                                                gl_contentPanel.createSequentialGroup()
                                                        .addComponent(modesWithDataPanel,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE).addGap(
                                                                18).addComponent(colorsList,
                                                                GroupLayout.PREFERRED_SIZE, 132,
                                                                GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        gl_contentPanel.setVerticalGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        gl_contentPanel.createSequentialGroup().addComponent(lblNewLabel)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(
                                        tagTextField, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(
                                        lblMessageTextTo).addPreferredGap(
                                        ComponentPlacement.RELATED).addComponent(messageTextField,
                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE).addPreferredGap(
                                        ComponentPlacement.UNRELATED).addComponent(lblPidsToFilter)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(
                                        pidTextField, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(
                                        lblLogLevel).addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(logLevelList, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                                .addGroup(
                                        gl_contentPanel.createParallelGroup(Alignment.LEADING)
                                                .addComponent(modesWithDataPanel,
                                                        Alignment.TRAILING,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE).addGroup(
                                                        Alignment.TRAILING,
                                                        gl_contentPanel.createSequentialGroup()
                                                                .addComponent(colorsList,
                                                                        GroupLayout.PREFERRED_SIZE,
                                                                        GroupLayout.DEFAULT_SIZE,
                                                                        GroupLayout.PREFERRED_SIZE)
                                                                .addGap(31)))));

        modesPanel = new FilteringModesPanel();
        modesWithDataPanel.add(modesPanel);
        modesPanel.setModeChangedListener(modeListener);
        contentPanel.setLayout(gl_contentPanel);
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onPositiveResult();
                    }
                });
            }
            {
                JButton cancelButton = new JButton("Cancel");
                buttonPane.add(cancelButton);
                cancelButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onNegativeResult();
                    }
                });
            }
        }
    }

    protected abstract void onPositiveResult();

    protected abstract void onNegativeResult();

    public String[] getTags() {
        String tagsString = tagTextField.getText();
        if (StringUtils.isNotBlank(tagsString)) {
            String[] tags = StringUtils.split(tagsString, ',');
            for (int i = 0; i < tags.length; ++i) {
                tags[i] = tags[i].trim();
            }
            return tags;
        }
        return null;
    }

    public String getMessageText() {
        String message = messageTextField.getText();
        if (StringUtils.isNotBlank(message)) {
            return message;
        }
        return null;
    }

    public int[] getPids() {
        String pidString = pidTextField.getText();
        if (StringUtils.isNotBlank(pidString)) {
            String[] pidStrings = StringUtils.split(pidString, ',');
            int[] pids = new int[pidStrings.length];
            for (int i = 0; i < pids.length; ++i) {
                pids[i] = Integer.parseInt(pidStrings[i].trim());
            }
            return pids;
        }
        return null;
    }

    public LogRecord.Priority getPriority() {
        return (Priority) logLevelList.getSelectedItem();
    }

    public FilteringMode getFilteringMode() {
        return modesPanel.getSelectedMode();
    }

    private class PriorityComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private Object selected;

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = anItem;
        }

        @Override
        public Object getElementAt(int index) {
            if (index == 0) {
                return null;
            }
            return LogRecord.Priority.values()[index - 1];
        }

        @Override
        public int getSize() {
            return LogRecord.Priority.values().length + 1;
        }

    }

    protected boolean isInputValid() {
        try {
            getPids();
            String request = getMessageText();
            if (SearchStrategyFactory.isSearchRequestValid(request)) {
                return true;
            } else {
                ErrorDialogsHelper.showError(this, "%s is not a valid search expression: %s",
                        request, SearchStrategyFactory.describeError(request));
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

    }

    protected JTextField getTagTextField() {
        return tagTextField;
    }

    protected JTextField getMessageTextField() {
        return messageTextField;
    }

    protected JTextField getPidTextField() {
        return pidTextField;
    }

    protected JComboBox getLogLevelList() {
        return logLevelList;
    }

    protected FilteringModesPanel getModePanel() {
        return modesPanel;
    }

    protected JComboBox getColorsList() {
        return colorsList;
    }

    private class ColorsComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private Object selected;

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = anItem;
        }

        @Override
        public Object getElementAt(int index) {
            return "<html><span style='background-color: "
                    + toString(Configuration.ui.highlightColors()[index]) + "'>Color " + index
                    + "</span></html>";

        }

        private String toString(Color color) {
            return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
        }

        @Override
        public int getSize() {
            return Configuration.ui.highlightColors().length;
        }
    }

    public Color getSelectedColor() {
        if (getFilteringMode() == FilteringMode.HIGHLIGHT) {
            return Configuration.ui.highlightColors()[colorsList.getSelectedIndex()];
        } else {
            return null;
        }
    }

    protected void setSelectedColor(Color color) {
        int index = 0;
        for (Color current : Configuration.ui.highlightColors()) {
            if (current.equals(color)) {
                colorsList.setSelectedIndex(index);
                return;
            } else {
                ++index;
            }
        }
    }

    public Object getAdditionalData() {
        return getSelectedColor();
    }

}
