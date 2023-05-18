package com.example.demo1;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    List<User> userData;

    List<String> filePaths = new ArrayList<>();

    List<User> removedUsers = new ArrayList<>();

    @FXML
    TableView<User> userTable;

    @FXML
    TableView<User> passedTable;

    @FXML
    TableColumn<User, String> firstName;

    @FXML
    TableColumn<User, String> lastName;

    @FXML
    TableColumn<User, String> email;

    @FXML
    TableColumn<User, String> imagelink;

    @FXML
    TableColumn<User, String> ip_address;

    @FXML
    TableColumn<User, String> passedFirstName;

    @FXML
    TableColumn<User, String> passedLastName;

    @FXML
    TableColumn<User, String> passedEmail;

    @FXML
    TableColumn<User, String> passedImageLink;

    @FXML
    TableColumn<User, String> passedIp;


    ObservableList<User> observableList;

    ObservableList<User> passedObservableList;

    private static final String path = "C:/Java/6_laboras/MOCK_DATA.csv";

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        firstName.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        lastName.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        email.setCellValueFactory(new PropertyValueFactory<User, String>("email"));
        imagelink.setCellValueFactory(new PropertyValueFactory<User, String>("imagelink"));
        ip_address.setCellValueFactory(new PropertyValueFactory<User, String>("ip_address"));

        passedFirstName.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        passedLastName.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        passedEmail.setCellValueFactory(new PropertyValueFactory<User, String>("email"));
        passedImageLink.setCellValueFactory(new PropertyValueFactory<User, String>("imagelink"));
        passedIp.setCellValueFactory(new PropertyValueFactory<User, String>("ip_address"));

        userData = new ArrayList<>();
        observableList = FXCollections.observableArrayList();
        passedObservableList = FXCollections.observableArrayList();

        userTable.setItems(observableList);
        passedTable.setItems(passedObservableList);
        loadInitialData();
    }

    public void loadInitialData()
    {
        Thread loadData = new Thread(() -> {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            boolean isFirstLine = true;

            while((line = reader.readLine()) != null)
            {
                if(isFirstLine)
                {
                    isFirstLine = false;
                    continue;
                }

                String[]columns = line.split(";");

                User user = new User();
                user.setFirstName(columns[0]);
                user.setLastName(columns[1]);
                user.setEmail(columns[2]);
                user.setImagelink(columns[3]);
                user.setIp_address(columns[4]);
                user.setId(userData.size());

                // Add the user to the list in a thread-safe manner
                synchronized (userData) {
                    userData.add(user);
                }

                // Update the UI in the JavaFX Application Thread
                Platform.runLater(() -> observableList.add(user));

                Thread.sleep(5);
            }

        } catch(IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        });
        loadData.start();
    }

    @FXML
    public void onLoadDataButtonPressed()
    {

        if(removedUsers.size() > 1)
        {
            userData.addAll(removedUsers);
            removedUsers.clear();
        }

       Thread thread1 = new Thread(() -> {

        for(int i = 0; i < userData.size(); i++)
        {
            int lastIpIndex = userData.get(i).getIp_address().lastIndexOf('.');
            int endIpIndex = userData.get(i).getIp_address().length();

            int firstNameLength = userData.get(i).getFirstName().length();
            int lastNameLength = userData.get(i).getLastName().length();

            final int index = i;

            String firstNameSubstring = firstNameLength >= 3 ? userData.get(i).getFirstName().substring(0, 3) : userData.get(i).getFirstName();
            String lastNameSubstring = lastNameLength >= 3 ? userData.get(i).getLastName().substring(0, 3) : userData.get(i).getLastName();

            String fileName = "C:/Java/6_laboras/files/" + firstNameSubstring + lastNameSubstring + userData.get(i).getIp_address().substring(lastIpIndex+1,endIpIndex) + ".csv";

            try
            {
                BufferedWriter writer = new BufferedWriter (new FileWriter(fileName));
                writer.write(userData.get(i).getFirstName() + ";" +
                        userData.get(i).getLastName() + ";" +
                        userData.get(i).getEmail() + ";" +
                        userData.get(i).getImagelink() + ";" +
                        userData.get(i).getIp_address());
                writer.newLine();
                writer.close();

                Platform.runLater(() -> {
                    filePaths.add(fileName);
                    observableList.remove(userData.get(index));
                    passedObservableList.add(userData.get(index));
                });
                Thread.sleep(5);
            } catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
                System.out.println("Error:" + e);
            }

        }
        });
       thread1.start();
    }
    @FXML
    public void onRemoveRowPressed(ActionEvent event) throws IOException {
        if(passedTable.getSelectionModel().getSelectedItem() != null)
        {
            User user = passedTable.getSelectionModel().getSelectedItem();
            Path path = Paths.get(filePaths.get(user.getId()));

            passedObservableList.remove(user);
            Files.delete(path);
            observableList.add(user);
            userData.remove(user);
            removedUsers.add(user);

            System.out.println("Removed: " +  user.getFirstName() + " "
            + user.getLastName() + "; File deleted: " + filePaths.get(user.getId()));
        }
    }
    @FXML
    public void onRemoveAllPressed(ActionEvent event) {
        Thread thread2 = new Thread(() -> {
            for(int i = 0; i < userData.size(); i++)
            {
                for(int j = 0; j < removedUsers.size(); j++)
                {
                    if(userData.get(i).getId() == removedUsers.get(j).getId())
                    {
                        break;
                    } else
                    {
                        final int index = i;
                        Path path = Paths.get(filePaths.get(userData.get(i).getId()));

                        Platform.runLater(() -> {
                            passedObservableList.remove(userData.get(index));
                            observableList.add(userData.get(index));
                            try {
                                    Files.delete(path);
                            } catch (IOException e) {
                                System.out.println("Warning: " + e + " Tried deleting inexistent item");
                            }
                        });
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                }
            }
        });
        thread2.start();

        System.out.println(removedUsers.size());
    }
}