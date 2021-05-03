package gui;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class SellerFormController implements Initializable {

    private SellerService service;

    private DepartmentService departmentService;

    private Seller entity;

    private List<DataChangeListener> dataChangeListener = new ArrayList<>();

    @FXML
    private ComboBox<Department> comboBoxDepartment;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEmail;

    @FXML
    private DatePicker dpBirthDate;

    @FXML
    private TextField txtBaseSalary;

    @FXML
    private Label labelErrorName;

    @FXML
    private Label labelErrorEmail;

    @FXML
    private Label labelErrorBirthDate;

    @FXML
    private Label labelErrorSalary;

    @FXML
    private Button buttonSave;

    @FXML
    private Button buttonCancel;

    private ObservableList<Department> observableList;

    public void loadAssociatedObjects(){
        if(departmentService == null){
            throw new IllegalStateException("DepartmentService was null");
        }
        List<Department> list = departmentService.findAll();
        observableList = FXCollections.observableArrayList(list);
        comboBoxDepartment.setItems(observableList);

    }

    public void subscribeChangeListener(DataChangeListener listener){
        dataChangeListener.add(listener);
    }

    @FXML
    public void onButtonSaveAction(ActionEvent event){
        if(entity == null){
            throw new IllegalStateException("Entity was null");
        }
        if(service == null){
            throw new IllegalStateException("Service was null");
        }
        try {
            entity = getFormData();
            service.savedOrUpdate(entity);
            notifyDataChangeListeners();
            Utils.currentStage(event).close();
        }
        catch (DbException dbException){
            Alerts.showAlerts("Error saving object",null,dbException.getMessage(), Alert.AlertType.ERROR);
        }
        catch (ValidationException validationException){
            setErrorMessages(validationException.getErrors());
        }
    }

    private void notifyDataChangeListeners() {
        for (DataChangeListener listener : dataChangeListener){
            listener.onDataChanged();
        }
    }

    private Seller getFormData() {
        Seller obj = new Seller();

        ValidationException exception = new ValidationException("Validation error");

        obj.setId(Utils.tryParseToInt(txtId.getText()));
        if(txtName.getText() == null || txtName.getText().trim().equals("")){
            exception.addError("name","Field can't be empty");
        }
        obj.setName(txtName.getText());
        if(exception.getErrors().size() > 0){
            throw exception;
        }
        return obj;
    }

    @FXML
    public void onButtonCancelAction(ActionEvent event){
       Utils.currentStage(event).close();

    }

    public void setSeller(Seller entity){
        this.entity = entity;
    }

    public void setServices(SellerService service, DepartmentService departmentService){
        this.service = service;
        this.departmentService = departmentService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeNodes();
    }


    //Controlador básico.

    private void initializeNodes(){
        Constraints.setTextFieldInteger(txtId);
        Constraints.setTextFieldMaxLength(txtName,70);
        Constraints.setTextFieldDouble(txtBaseSalary);
        Constraints.setTextFieldMaxLength(txtEmail,60);
        Utils.formatDatePicker(dpBirthDate,"dd/MM/yyyy");
        initializerComboBoxDepartment();
    }

    public void updateFormData(){
        if(entity == null) {
            throw new IllegalStateException("Entity was null");
        }
        txtId.setText(String.valueOf(entity.getId()));
        txtName.setText(entity.getName());
        txtEmail.setText(entity.getEmail());
        Locale.setDefault(Locale.US);
        txtBaseSalary.setText(String.format("%.2f",entity.getBaseSalary()));

        dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
        if(entity.getDepartment() == null){
            comboBoxDepartment.getSelectionModel().selectFirst();
        }else {
            comboBoxDepartment.setValue(entity.getDepartment());
        }
    }

    private void setErrorMessages(Map<String, String> errors){
        Set<String> fields = errors.keySet();

        if(fields.contains("name")){
            labelErrorName.setText(errors.get("name"));
        }
    }

    private void initializerComboBoxDepartment(){
        Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>(){
            @Override
            protected void updateItem(Department item, boolean empty){
                super.updateItem(item,empty);
                setText(empty ? "" : item.getName());
            }
        };

        comboBoxDepartment.setCellFactory(factory);
        comboBoxDepartment.setButtonCell(factory.call(null));
    }

}
