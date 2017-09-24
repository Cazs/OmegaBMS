/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

/**
 *
 * @author ghost
 */
public enum Screens 
{
    HOME("Homescreen.fxml"),
    LOGIN("Login.fxml"),
    OPERATIONS("Operations.fxml"),
    OPERATIONS_PRODUCTION("Operations_production.fxml"),
    OPERATIONS_SALES("Operations_sales.fxml"),
    OPERATIONS_FACILITIES("Operations_facilities.fxml"),
    OPERATIONS_CLIENTS("Operations_facilities.fxml"),
    SAFETY("Safety.fxml"),
    SAFETY_FILES("SafetyFiles.fxml"),
    SETTINGS("Settings.fxml"),
    CREATE_ACCOUNT("Create_account.fxml"),
    RESET_PWD("ResetPassword.fxml"),
    NEW_QUOTE("New_quote.fxml"),
    GENERIC_QUOTES("PendingQuotes.fxml"),
    REJECTED_QUOTES("RejectedQuotes.fxml"),
    NEW_GENERIC_QUOTE("New_generic_quote.fxml"),
    QUOTES("Quotes.fxml"),
    VIEW_QUOTE("View_quote.fxml"),
    VIEW_GENERIC_QUOTE("View_generic_quote.fxml"),
    SALES("Sales.fxml"),
    JOBS("Jobs.fxml"),
    VIEW_JOB("View_job.fxml"),
    CLIENTS("Clients.fxml"),
    NEW_CLIENT("NewClient.fxml"),
    SUPPLIERS("Suppliers.fxml"),
    NEW_SUPPLIER("NewSupplier.fxml"),
    RESOURCES("Stock.fxml"),
    NEW_RESOURCE("NewResource.fxml"),
    FACILITIES("Facilities.fxml"),
    HR("HR.fxml"),
    ACCOUNTING("Accounting.fxml"),
    PURCHASES("Purchases.fxml"),
    ASSETS("Assets.fxml"),
    NEW_ASSET("NewAsset.fxml");

    private String screen;
    
    Screens(String screen){
        this.screen = screen;
    }
    
    public String getScreen()
    {
        return screen;
    }
}