package com.esmooc;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;


public class RequestToolV2 extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()  -> {
            RequestToolV2 tool = new RequestToolV2();
            tool.setVisible(true);
        });
    }



    /// 测试
    private final JTabbedPane tabbedPane;
    private final JComboBox<String> methodComboBox;
    private final JTextField urlTextField;
    private final JButton sendButton;
    private JPanel contentPanel;


    private JPanel createTablePanel(String title, String[] columnNames) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new  Font("Arial", Font.BOLD, 14));
        panel.add(titleLabel,  BorderLayout.NORTH);
        JTable table = createFormDataTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());


        panel.add(scrollPane,  BorderLayout.CENTER);

        return panel;
    }

    public RequestToolV2() {
        this.setTitle("网络请求工具");
        this.setSize(800,  600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new  BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new  FlowLayout(FlowLayout.LEFT));

        methodComboBox = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE"});
        urlTextField = new JTextField("http://example.com",  30);
        sendButton = new JButton("发送");

        topPanel.add(new  JLabel("请求方式: "));
        topPanel.add(methodComboBox);
        topPanel.add(new  JLabel("URL: "));
        topPanel.add(urlTextField);
        topPanel.add(sendButton);

        mainPanel.add(topPanel,  BorderLayout.NORTH);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("请求头",  createHeadersPanel());
        tabbedPane.addTab("Params",  createParamsPanel());
        tabbedPane.addTab("Cookies",  createCookiesPanel());
        tabbedPane.addTab("Body",  createRequestBodyPanel());
        mainPanel.add(tabbedPane,  BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        mainPanel.add(statusPanel,  BorderLayout.SOUTH);

        this.setContentPane(mainPanel);

        sendButton.addActionListener(new  ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest();
            }
        });
    }

    private JPanel createHeadersPanel() {
        return createTablePanel("Headers", new String[]{"Header Name", "Header Value"});
    }

    private JPanel createParamsPanel() {
        return createTablePanel("Params", new String[]{"Param Name", "Param Value"});
    }

    private JPanel createCookiesPanel() {
        return createTablePanel("Cookies", new String[]{"Cookie Name", "Cookie Value"});
    }

    private JPanel createRequestBodyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new  BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JComboBox<String> contentTypeComboBox = new JComboBox<>(new String[]{"form-data", "raw"});
        contentTypeComboBox.addActionListener(new  ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCardBodyContent(contentTypeComboBox.getSelectedItem(),  contentPanel);
            }
        });
        panel.add(contentTypeComboBox,  BorderLayout.NORTH);

        if (contentPanel==null){
            contentPanel = new JPanel();
        }

        contentPanel.setLayout(new  CardLayout());

        JTable formDataTable = createFormDataFileTable();
        JScrollPane formDataScrollPane = new JScrollPane(formDataTable);

        JTextArea rawTextArea = new JTextArea();
        rawTextArea.setLineWrap(true);
        rawTextArea.setRows(10);
        rawTextArea.setBorder(BorderFactory.createEmptyBorder(5,  5, 5, 5));

        contentPanel.add(formDataScrollPane,  "form-data");
        contentPanel.add(rawTextArea,  "raw");

        panel.add(contentPanel,  BorderLayout.CENTER);

        updateCardBodyContent("form-data", contentPanel);

        return panel;
    }


    private void updateCardBodyContent(Object selectedItem, JPanel contentPanel) {
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        cardLayout.show(contentPanel,  selectedItem.toString());
    }


    private JTable createFormDataTable() {
        String[] columns = {"名称", "参数", "操作"};

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        model.addRow(new Object[columns.length]);

        JTable table = new JTable(model){
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return JButton.class; // 第三列是按钮列
                    default:
                        return super.getColumnClass(column);
                }
            }
        };

        table.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox()));


        table.setRowHeight(30);
        table.setPreferredScrollableViewportSize(new  Dimension(100, 100));
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);
        table.setIntercellSpacing(new  Dimension(0, 0));
        table.setBorder(BorderFactory.createMatteBorder(1,  1, 1, 1, Color.BLACK));
        table.setRowMargin(0);

        table.addMouseListener(new  MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()  == 2) {
                    int row = table.getSelectedRow();
                    int column = table.getSelectedColumn();
                    if (row != -1 && column != -1) {
                        table.editCellAt(row,  column);
                    }
                }
            }
        });
        model.addTableModelListener(e  -> {
            int lastRow = model.getRowCount()  - 1;
            boolean lastRowNotEmpty =
                    model.getValueAt(lastRow,  0) != null &&
                            !model.getValueAt(lastRow,  0).toString().trim().isEmpty();
            if (lastRowNotEmpty) {
                model.addRow(new  Object[]{"", ""});
            }
        });

        return table;
    }


    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("删除");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    // 自定义列编辑器
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("删除");
            button.addActionListener(e -> {
                JButton sourceButton = (JButton) e.getSource();
                JTable table =(JTable) SwingUtilities.getAncestorOfClass(JTable.class, sourceButton);
                if (table != null) {
                    int row = table.convertRowIndexToModel(table.getEditingRow());
                TableModel model = table.getModel();
                if (model instanceof DefaultTableModel) {
                    ((DefaultTableModel) model).removeRow(row);
                } else if (model instanceof FormTable) {
                    FormTable formTable = (FormTable) model;
                    formTable.removeRowAt(row);
                } else {
                    System.out.println("无法支持的表格模型类型");
                }
                } else {
                    System.out.println("无法找到 JTable 组件");
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return button;
        }
    }
    private void sendRequest() {
        stopTableEditing();
        String method = (String) methodComboBox.getSelectedItem();
        String url = urlTextField.getText();
        Map<String, String> headers = getHeadersFromTable(getTableFromTabbedPane(0));
        Map<String, String> params = getParamsFromTable(getTableFromTabbedPane(1));
        Map<String, String> cookies = getCookiesFromTable(getTableFromTabbedPane(2));
        String requestBody = getRequestBodyData();
        System.out.println("请求方式: " + method);
        System.out.println("URL: " + url);
        System.out.println("Headers:");
        headers.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("Params:");
        params.forEach((key, value) -> System.out.println(key + "=" + value));
        System.out.println("Cookies:");
        cookies.forEach((key, value) -> System.out.println(key + "=" + value));
        System.out.println("Request Body: " + requestBody);
    }

    private void stopTableEditing() {
        JTable headersTable = getTableFromTabbedPane(0);
        JTable paramsTable = getTableFromTabbedPane(1);
        JTable cookiesTable = getTableFromTabbedPane(2);
        if (headersTable != null && headersTable.isEditing()) {
            headersTable.getCellEditor().stopCellEditing();
        }
        if (paramsTable != null && paramsTable.isEditing()) {
            paramsTable.getCellEditor().stopCellEditing();
        }
        if (cookiesTable != null && cookiesTable.isEditing()) {
            cookiesTable.getCellEditor().stopCellEditing();
        }
        JPanel bodyPanel = (JPanel) tabbedPane.getComponentAt(3);
        if (bodyPanel != null) {
            JComboBox<String> contentTypeComboBox = (JComboBox<String>) bodyPanel.getComponent(0);
            String contentType = (String) contentTypeComboBox.getSelectedItem();
            if ("form-data".equals(contentType)) {
                // 修改这部分代码来正确获取 form-data 表格
                JPanel contentPanel = (JPanel) bodyPanel.getComponent(1);
                JScrollPane scrollPane = findScrollPane(contentPanel);
                if (scrollPane != null) {
                    JTable formDataTable = (JTable) scrollPane.getViewport().getView();
                    if (formDataTable != null && formDataTable.isEditing()) {
                        formDataTable.getCellEditor().stopCellEditing();
                    }
                }
            } else if ("raw".equals(contentType)) {
                // 同时处理 raw 文本区域的情况
                JPanel contentPanel = (JPanel) bodyPanel.getComponent(1);
                JTextArea rawTextArea = findTextArea(contentPanel);
                if (rawTextArea != null) {
                    rawTextArea.requestFocus(); // 触发失焦以确保内容更新
                }
            }
        }
    }

    private String getRequestBodyData() {
        String contentType = (String) ((JComboBox<?>) ((JPanel) tabbedPane.getComponentAt(3)).getComponent(0)).getSelectedItem();

        if ("form-data".equals(contentType)) {
            JPanel contentPanel = (JPanel) ((JPanel) tabbedPane.getComponentAt(3)).getComponent(1);
            JScrollPane scrollPane = findScrollPane(contentPanel);
            if (scrollPane != null) {
                JTable formDataTable = (JTable) scrollPane.getViewport().getView();
                Map<String, Object> formData = getFormDataFromTable(formDataTable);
                return JSONUtil.toJsonStr(formData);
            }
        } else if ("raw".equals(contentType)) {
            JPanel contentPanel = (JPanel) ((JPanel) tabbedPane.getComponentAt(3)).getComponent(1);
            JTextArea rawTextArea = findTextArea(contentPanel);
            if (rawTextArea != null) {
                return rawTextArea.getText();
            }
        }

        return "";
    }


    private JScrollPane findScrollPane(Container container) {
        for (Component component : container.getComponents())  {
            if (component instanceof JScrollPane) {
                return (JScrollPane) component;
            }
        }
        return null;
    }

    private JTextArea findTextArea(Container container) {
        for (Component component : container.getComponents())  {
            if (component instanceof JTextArea) {
                return (JTextArea) component;
            }
        }
        return null;
    }



    private JTable getTableFromTabbedPane(int index) {
        JPanel tabPanel = (JPanel) tabbedPane.getComponentAt(index);
        for (Component component : tabPanel.getComponents())  {
            if (component instanceof JScrollPane scrollPane) {
                return (JTable) scrollPane.getViewport().getView();
            }
        }
        return null;
    }

    private Map<String, String> getHeadersFromTable(JTable table) {
        Map<String, String> headers = new HashMap<>();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int i = 0; i < model.getRowCount();  i++) {
            String name = (String) model.getValueAt(i,  0);
            String value = (String) model.getValueAt(i,  1);
            if (StrUtil.isNotEmpty(name)){
                headers.put(name,  value);
            }
        }
        return headers;
    }

    private Map<String, String> getParamsFromTable(JTable table) {
        Map<String, String> params = new HashMap<>();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        for (int i = 0; i < model.getRowCount();  i++) {
            String name = (String) model.getValueAt(i,  0);
            String value = (String) model.getValueAt(i,  1);
            if (StrUtil.isNotEmpty(name)){
                params.put(name,  value);
            }
        }
        return params;
    }

    private Map<String, String> getCookiesFromTable(JTable table) {
        Map<String, String> cookies = new HashMap<>();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        for (int i = 0; i < model.getRowCount();  i++) {
            String name = (String) model.getValueAt(i,  0);
            String value = (String) model.getValueAt(i,  1);
            if (StrUtil.isNotEmpty(name)){
                cookies.put(name,  value);
            }

        }

        return cookies;
    }


    private Map<String, Object> getFormDataFromTable(JTable table) {
        Map<String, Object> data = new HashMap<>();
        TableModel model = table.getModel();
        for (int i = 0; i < model.getRowCount();  i++) {
            String name = (String) model.getValueAt(i,  0);
            String type = (String) model.getValueAt(i,  1);
            String  value = (String) model.getValueAt(i,  2);
            System.out.println(name +"  " + value+"    " + type);
            if ("file".equals(type)){

                if (FileUtil.exist(value)){
                    data.put(name,  new File(value));
                }else {
                    data.put(name,  null);
                }


            }else {
                if (StrUtil.isNotEmpty(name)){
                    data.put(name,  value);
                }
            }
        }
        return data;
    }





    static class FormTable extends AbstractTableModel {
        private List<Object[]> data = new ArrayList<>();
        private final String[] columnNames = {"名称", "类型", "值","操作"};

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        public void addRow(Object... row) {
            data.add(row);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return data.isEmpty() ? 0 : data.get(0).length;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return data.get(row)[column];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            data.get(row)[column] = value;
            fireTableCellUpdated(row, column);
        }

    public void removeRowAt(int row) {
        if (row >= 0 && row < data.size()) {
            data.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    }


    private JTable createFormDataFileTable() {
        FormTable model = new FormTable();
        model.addRow(new Object[]{"", "string", null, ""});

        JTable table = new JTable(model) {
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Object.class; // 值列可能包含不同类型的数据
                    case 3:
                        return JButton.class; // 第四列是按钮列
                    default:
                        return super.getColumnClass(column);
                }
            }
        };

        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox()));

        table.setRowHeight(30);
        table.setPreferredScrollableViewportSize(new Dimension(100, 100));
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
        table.setRowMargin(0);


        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                if (row == model.getRowCount() - 1) { // 检查最后一行
                    Object name = model.getValueAt(row, 0);
                    Object value = model.getValueAt(row, 2);
                    boolean hasData = (name != null && !name.toString().trim().isEmpty())
                            || (value != null && !value.toString().trim().isEmpty());
                    if (hasData) {
                        model.addRow(new Object[]{"", "string", null, ""}); // 自动添加新行
                    }
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    int column = table.columnAtPoint(e.getPoint());
                    if (row != -1 && column != -1) {
                        table.editCellAt(row, column);
                    }
                }
            }
        });

        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"string", "file"})));

        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private String selectedFilePath;
            private JLabel fileLabel;
            private JButton selectButton;
            private JFileChooser fileChooser;
            private int currentRow = -1;
            private int currentColumn = -1;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                String type = (String) table.getValueAt(row, 1);
                Component editorComponent = super.getTableCellEditorComponent(table, value, isSelected, row, column);

                JPanel panel = new JPanel(new BorderLayout());
                panel.setOpaque(false);

                if ("file".equals(type)) {
                    if (fileLabel == null) {
                        fileLabel = new JLabel();
                        fileLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    } else {
                        if (value == null || value.toString().isEmpty()) {
                            fileLabel.setText("未选择文件");
                        } else {
                            fileLabel.setText(value.toString());
                        }
                    }

                    if (fileChooser == null) {
                        fileChooser = new JFileChooser();
                    }

                    if (selectButton == null) {
                        selectButton = new JButton("选择文件");
                        selectButton.addActionListener(e -> {
                            int result = fileChooser.showOpenDialog(null);
                            if (result == JFileChooser.APPROVE_OPTION) {
                                selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                                File file = fileChooser.getSelectedFile();
                                if (file != null )  {
                                    fileLabel.setText(file.getName());
                                    fireEditingStopped();
                                }else {
                                    JOptionPane.showMessageDialog(table,  "文件不存在！", "错误", JOptionPane.ERROR_MESSAGE);
                                    fileLabel.setText(null);
                                }

                            }
                        });
                    }

                    panel.add(fileLabel, BorderLayout.CENTER);
                    panel.add(selectButton, BorderLayout.EAST);

                    currentRow = table.convertRowIndexToModel(row);
                    currentColumn = table.convertColumnIndexToModel(column);
                } else if ("string".equals(type)) {
                    if (editorComponent == null) {
                        editorComponent = new JTextField(value != null ? value.toString() : "");
                    }
                    panel.add(editorComponent, BorderLayout.CENTER);

                    currentRow = table.convertRowIndexToModel(row);
                    currentColumn = table.convertColumnIndexToModel(column);
                }
                return panel;
            }

            @Override
            public Object getCellEditorValue() {
                String type = (String) table.getValueAt(currentRow, 1);
                if ("file".equals(type)) {
                    return selectedFilePath;
                } else {
                    return super.getCellEditorValue();
                }
            }

            @Override
            public boolean stopCellEditing() {
                if (fileLabel != null) {
                    selectedFilePath = fileLabel.getText().isEmpty() ? null : fileLabel.getText();
                }
                if (currentRow != -1 && currentColumn != -1) {
                    table.getModel().setValueAt(selectedFilePath, currentRow, currentColumn);
                }
                return super.stopCellEditing();
            }
        });

        return table;
    }
}
