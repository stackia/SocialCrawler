//package view;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonParser;
//import org.jdom2.Document;
//import org.jdom2.Element;
//import org.jdom2.input.SAXBuilder;
//
//import javax.swing.*;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import javax.swing.table.AbstractTableModel;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.StringReader;
//import java.util.Collection;
//import java.util.List;
//import java.util.Vector;
//
///**
// * Project: QQSpider
// * Package: view
// * Created by Stackia <jsq2627@gmail.com> on 10/30/14.
// */
//public class MainForm extends JFrame {
//
//    /* UI Components */
//    private JPanel rootPanel;
//    private JTextField uinTextField;
//    private JTextField skeyTextField;
//    private JButton runButton;
//    private JTable userTable;
//    private JLabel statusLabel;
//    private JButton stopButton;
//
//    private Vector<QQUser> userVector = new Vector<QQUser>();
//    private FetchDispatcher fetchDispatcher = new FetchDispatcher(WORKER_NUM);
//    private QueueTableModel queueTableModel = new QueueTableModel();
//    private SpinnerNumberModel depthSpinnerModel = new SpinnerNumberModel(1, 0, null, 1);
//
//    public MainForm() {
//        setTitle("QQ Spider");
//
//        /* TODO: Remove test data */
//        vUIN = "123395879";
//        vSKey = "@PKX8VDZMC";
//        uinTextField.setText(vUIN);
//        skeyTextField.setText(vSKey);
//        QQUser testUser = new QQUser();
//        testUser.uin = vUIN;
//        testUser.depth = 0;
//        addUser(testUser);
//
//        userTable.setModel(queueTableModel);
//        depthSpinner.setModel(depthSpinnerModel);
//        fetchDispatcher.setOnDispatcherStateChangedListener(this);
//
//        runButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                runButton.setText("...");
//                runButton.setEnabled(false);
//                switch (fetchDispatcher.getState()) {
//                    case FetchDispatcher.State.Paused:
//                        fetchDispatcher.start();
//                        break;
//                    case FetchDispatcher.State.Started:
//                        fetchDispatcher.pause();
//                        break;
//                }
//            }
//        });
//        uinTextField.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                vUIN = uinTextField.getText();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                vUIN = uinTextField.getText();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                vUIN = uinTextField.getText();
//            }
//        });
//        skeyTextField.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                vSKey = skeyTextField.getText();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                vSKey = skeyTextField.getText();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                vSKey = skeyTextField.getText();
//            }
//        });
//    }
//
//    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ignored) {
//        }
//        MainForm frame = new MainForm();
//        frame.setContentPane(frame.rootPanel);
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//    }
//
//    private void addUser(QQUser user) {
//        if (user.uin != null && !user.uin.isEmpty() && !userVector.contains(user)) {
//            if (user.depth > depthSpinnerModel.getNumber().intValue()) {
//                return;
//            }
//            userVector.add(user);
//            if (user.uin.equals(MainForm.vUIN)) {
//                fetchDispatcher.addRequest(new FetchRequest(user, FetchRequest.Type.Friends, this));
//            }
//            if (user.depth <= 1) {
//                fetchDispatcher.addRequest(new FetchRequest(user, FetchRequest.Type.RecentVisitors, this));
//            }
//            fetchDispatcher.addRequest(new FetchRequest(user, FetchRequest.Type.MessageBoard, this));
////            fetchDispatcher.addRequest(new core.crawler.FetchRequest(user, core.crawler.FetchRequest.Type.FullProfiles, this));
////            fetchDispatcher.addRequest(new core.crawler.FetchRequest(user, core.crawler.FetchRequest.Type.SimpleProfiles, this));
//        }
//    }
//
//    private void addUsers(Collection<QQUser> users) {
//        for (QQUser userToAdd : users) {
//            addUser(userToAdd);
//        }
//    }
//
//    @Override
//    public void onRequestFinished(FetchRequest request) {
//        QQUser user = request.getUser();
//        int nextDepth = user.depth + 1;
//        switch (request.getType()) {
//            case FetchRequest.Type.Friends:
//                user.friends = new Vector<QQUser>();
//                try {
//                    SAXBuilder saxBuilder = new SAXBuilder();
//                    Document document = saxBuilder.build(new StringReader(user.originalFriendsData));
//                    List<Element> groups = document.getRootElement().getChildren("group");
//                    for (Element group : groups) {
//                        List<Element> friends = group.getChildren("friend");
//                        for (Element friend : friends) {
//                            QQUser newUser = new QQUser();
//                            newUser.uin = friend.getAttributeValue("uin");
//                            newUser.depth = nextDepth;
//                            user.friends.add(newUser);
//                        }
//                    }
//                } catch (Exception ignored) {
//                 }
//                if (exportRelatedUsers(user)) {
//                    addUsers(user.relatedUsers);
//                }
//                break;
//            case FetchRequest.Type.RecentVisitors:
//                user.recentVisitors = new Vector<QQUser>();
//                try {
//                    JsonElement root = new JsonParser().parse(user.originalRecentVisitorsData);
//                    JsonArray jsonUserArray = root.getAsJsonObject().get("data").getAsJsonObject().get("items").getAsJsonArray();
//                    for (JsonElement jsonUser : jsonUserArray) {
//                        QQUser newUser = new QQUser();
//                        newUser.uin = jsonUser.getAsJsonObject().get("uin").getAsString();
//                        newUser.depth = nextDepth;
//                        user.recentVisitors.add(newUser);
//                    }
//                } catch (Exception ignored) {
//                }
//                if (exportRelatedUsers(user)) {
//                    addUsers(user.relatedUsers);
//                }
//                break;
//            case FetchRequest.Type.MessageBoard:
//                user.messageBoardUsers = new Vector<QQUser>();
//                try {
//                    JsonElement root = new JsonParser().parse(user.originalMessageBoardData);
//                    JsonArray jsonUserArray = root.getAsJsonObject().get("data").getAsJsonObject().get("commentList").getAsJsonArray();
//                    for (JsonElement jsonUser : jsonUserArray) {
//                        QQUser newUser = new QQUser();
//                        newUser.uin = jsonUser.getAsJsonObject().get("uin").getAsString();
//                        newUser.depth = nextDepth;
//                        user.messageBoardUsers.add(newUser);
//                    }
//                } catch (Exception ignored) {
//                }
//                if (exportRelatedUsers(user)) {
//                    addUsers(user.relatedUsers);
//                }
//                break;
//            case FetchRequest.Type.FullProfiles:
//
//                break;
//            case FetchRequest.Type.SimpleProfiles:
//
//                break;
//        }
//        queueTableModel.fireTableDataChanged();
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                statusLabel.setText("Current users: " + userVector.size());
//            }
//        });
//    }
//
//    private boolean exportRelatedUsers(QQUser user) {
//        Vector<QQUser> relatedUsers = new Vector<QQUser>();
//        if (user.uin.equals(vUIN)) {
//            if (user.friends == null) {
//                return false;
//            }
//        }
//        if (user.depth <= 1) {
//            if (user.recentVisitors == null) {
//                return false;
//            }
//        }
//        if (user.messageBoardUsers == null) {
//            return false;
//        }
//        if (user.friends != null) {
//            for (QQUser userToAdd : user.friends) {
//                if (!relatedUsers.contains(userToAdd))
//                    relatedUsers.add(userToAdd);
//            }
//        }
//        if (user.recentVisitors != null) {
//            for (QQUser userToAdd : user.recentVisitors) {
//                if (!relatedUsers.contains(userToAdd))
//                    relatedUsers.add(userToAdd);
//            }
//        }
//        if (user.messageBoardUsers != null) {
//            for (QQUser userToAdd : user.messageBoardUsers) {
//                if (!relatedUsers.contains(userToAdd))
//                    relatedUsers.add(userToAdd);
//            }
//        }
//        user.relatedUsers = relatedUsers;
//        return true;
//    }
//
//    @Override
//    public void onDispatcherStarted() {
//        runButton.setText("Pause");
//        runButton.setEnabled(true);
//    }
//
//    @Override
//    public void onDispatcherPaused() {
//        runButton.setText("Start");
//        runButton.setEnabled(true);
//    }
//
//    public class QueueTableModel extends AbstractTableModel {
//        @Override
//        public int getRowCount() {
//            return userVector.size();
//        }
//
//        @Override
//        public int getColumnCount() {
//            return 9;
//        }
//
//        @Override
//        public Object getValueAt(int rowIndex, int columnIndex) {
//            String value = "";
//            QQUser user = userVector.get(rowIndex);
//            switch (columnIndex) {
//                case 0:
//                    value = Integer.toString(user.depth);
//                    break;
//                case 1:
//                    value = user.uin;
//                    break;
//                case 2:
//                    if (user.relatedUsers != null) {
//                        if (user.relatedUsers.size() > 0) {
//                            value = "OK(" + user.relatedUsers.size() + ")";
//                        } else {
//                            value = "OK(None)";
//                        }
//                    }
//                    break;
//                case 3:
//                    if (user.profiles != null)
//                        value = "OK";
//                    break;
//                case 4:
//                    if (user.friends != null) {
//                        if (user.friends.size() > 0) {
//                            value = "OK(" + user.friends.size() + ")";
//                        } else {
//                            value = "OK(None)";
//                        }
//                    }
//                    break;
//                case 5:
//                    if (user.recentVisitors != null) {
//                        if (user.recentVisitors.size() > 0) {
//                            value = "OK(" + user.recentVisitors.size() + ")";
//                        } else {
//                            value = "OK(None)";
//                        }
//                    }
//                    break;
//                case 6:
//                    if (user.messageBoardUsers != null) {
//                        if (user.messageBoardUsers.size() > 0) {
//                            value = "OK(" + user.messageBoardUsers.size() + ")";
//                        } else {
//                            value = "OK(None)";
//                        }
//                    }
//                    break;
//                case 7:
//                    if (user.originalFullProfilesData != null && !user.originalFullProfilesData.isEmpty())
//                        value = "OK";
//                    break;
//                case 8:
//                    if (user.originalSimpleProfilesData != null && !user.originalSimpleProfilesData.isEmpty())
//                        value = "OK";
//                    break;
//            }
//            return value;
//        }
//
//        @Override
//        public String getColumnName(int column) {
//            String name = "";
//            switch (column) {
//                case 0:
//                    name = "Depth";
//                    break;
//                case 1:
//                    name = "UIN";
//                    break;
//                case 2:
//                    name = "Related Users";
//                    break;
//                case 3:
//                    name = "Profiles";
//                    break;
//                case 4:
//                    name = "Friends";
//                    break;
//                case 5:
//                    name = "Recent Visitors";
//                    break;
//                case 6:
//                    name = "Message Board";
//                    break;
//                case 7:
//                    name = "Full Profiles";
//                    break;
//                case 8:
//                    name = "Simple Profiles";
//                    break;
//            }
//            return name;
//        }
//    }
//}
