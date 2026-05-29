import com.mybank.domain.Bank;
import com.mybank.domain.Customer;
import com.mybank.domain.Account;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.SavingsAccount;

import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;
import java.io.IOException;

public class TUIDemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUSTOMER_INFO = 2010;

    public static void main(String[] args) throws Exception {
        TUIDemo demo = new TUIDemo();
        (new Thread(demo)).start();
    }

    public TUIDemo() throws Exception {
        super(BackendType.SWING);

        try {
            DataSource dataSource = new DataSource("data/test.dat");
            dataSource.loadData();
        } catch (IOException e) {
            System.err.println("An error occurred while loading test.dat: " + e.getMessage());
        }

        addToolMenu();

        TMenu fileMenu = addMenu("&File");
        fileMenu.addItem(CUSTOMER_INFO, "&Customer Info");
        fileMenu.addDefaultItem(TMenu.MID_SHELL);
        fileMenu.addSeparator();
        fileMenu.addDefaultItem(TMenu.MID_EXIT);

        addWindowMenu();

        TMenu helpMenu = addMenu("&Help");
        helpMenu.addItem(ABOUT_APP, "&About...");

        setFocusFollowsMouse(true);

        showCustomerDetails();
    }

    @Override
    protected boolean onMenu(TMenuEvent menu) {
        if (menu.getId() == ABOUT_APP) {
            messageBox("About", "\t\t\t\t\t   Just a simple Jexer demo.").show();
            return true;
        }
        if (menu.getId() == CUSTOMER_INFO) {
            showCustomerDetails();
            return true;
        }
        return super.onMenu(menu);
    }

    private void showCustomerDetails() {
        TWindow customerWindow = addWindow("Customer Window", 2, 1, 40, 10, TWindow.NOZOOMBOX);
        customerWindow.newStatusBar("Enter valid customer number and press Show...");
        customerWindow.addLabel("Enter customer number: ", 2, 2);

        TField customerNum = customerWindow.addField(24, 2, 3, false);
        TText details = customerWindow.addText("Owner Name: \nAccount Type: \nAccount Balance: ", 2, 4, 38, 8);

        customerWindow.addButton("&Show", 28, 2, new TAction() {
            @Override
            public void DO() {
                try {
                    int num = Integer.parseInt(customerNum.getText());

                    if (num < 0 || num >= Bank.getNumberOfCustomers()) {
                        messageBox("Error", "Customer with ID " + num + " not found!").show();
                        return;
                    }

                    Customer customer = Bank.getCustomer(num);
                    String accountType = "None";
                    double balance = 0.0;

                    if (customer.getNumberOfAccounts() > 0) {
                        Account account = customer.getAccount(0);
                        balance = account.getBalance();

                        if (account instanceof CheckingAccount) {
                            accountType = "Checking";
                        } else if (account instanceof SavingsAccount) {
                            accountType = "Savings";
                        }
                    }

                    String info = String.format(
                            "Owner Name: %s %s (id=%d)\nAccount Type: '%s'\nAccount Balance: $%.2f",
                            customer.getFirstName(), customer.getLastName(), num, accountType, balance
                    );

                    details.setText(info);

                } catch (NumberFormatException e) {
                    messageBox("Error", "You must provide a valid integer customer number!").show();
                } catch (Exception e) {
                    messageBox("Error", "An unexpected error occurred: " + e.getMessage()).show();
                }
            }
        });
    }
}