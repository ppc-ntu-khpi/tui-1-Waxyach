import com.mybank.domain.Bank;
import com.mybank.domain.Customer;
import com.mybank.domain.Account;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.SavingsAccount;
import com.mybank.domain.OverDraftAmountException;

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
    private static final int CUSTOMER_REPORT = 2020;

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
        fileMenu.addItem(CUSTOMER_REPORT, "Customer &Report");
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
        switch (menu.getId()) {
            case ABOUT_APP -> {
                messageBox("About", "\t\t\t\t\t   Just a simple Jexer demo.").show();
                return true;
            }
            case CUSTOMER_INFO -> {
                showCustomerDetails();
                return true;
            }
            case CUSTOMER_REPORT -> {
                showCustomerReport();
                return true;
            }
        }
        return super.onMenu(menu);
    }

    private void showCustomerDetails() {
        TWindow customerWindow = addWindow("Customer Window", 2, 1, 40, 12, TWindow.NOZOOMBOX);
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

        customerWindow.addLabel("Select Account Index:", 2, 8);
        TField accountNumField = customerWindow.addField(24, 8, 3, false);

        customerWindow.addButton("&Manage Selected", 2, 9, new TAction() {
            @Override
            public void DO() {
                try {
                    int customerIdx = Integer.parseInt(customerNum.getText());
                    int accIdx = Integer.parseInt(accountNumField.getText());

                    if (customerIdx < 0 || customerIdx >= Bank.getNumberOfCustomers()) {
                        messageBox("Error", "Customer not found!").show();
                        return;
                    }

                    Customer c = Bank.getCustomer(customerIdx);
                    if (accIdx >= 0 && accIdx < c.getNumberOfAccounts()) {
                        showAccountActions(c, accIdx);
                    } else {
                        messageBox("Error", "Invalid account index!").show();
                    }
                } catch (NumberFormatException e) {
                    messageBox("Error", "Please enter valid Customer and Account IDs!").show();
                }
            }
        });
    }

    private void showCustomerReport() {
        TWindow reportWindow = addWindow("Customer Report", 2, 1, 50, 15, TWindow.NOZOOMBOX);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            Customer c = Bank.getCustomer(i);
            sb.append("Customer: ").append(c.getFirstName()).append(" ").append(c.getLastName()).append("\n");

            for (int j = 0; j < c.getNumberOfAccounts(); j++) {
                Account a = c.getAccount(j);
                sb.append(" - Account ").append(j).append(": $").append(a.getBalance()).append("\n");
            }
            sb.append("----------------------------\n");
        }
        reportWindow.addText(sb.toString(), 2, 2, 45, 10);
    }

    private void showAccountActions(Customer customer, int accountIndex) {
        Account account = customer.getAccount(accountIndex);

        TWindow actionWindow = addWindow("Account: " + accountIndex, 10, 5, 30, 8);

        actionWindow.addButton("&Deposit", 2, 2, new TAction() {
            @Override
            public void DO() {
                String amountStr = inputBox("Deposit", "Enter amount:").getText();
                try {
                    double amt = Double.parseDouble(amountStr);
                    if (amt > 0) {
                        account.deposit(amt);
                        messageBox("Success", "New balance: $" + account.getBalance()).show();
                    } else {
                        messageBox("Error", "Amount must be positive").show();
                    }
                } catch (NumberFormatException e) {
                    messageBox("Error", "Invalid amount").show();
                }
            }
        });

        actionWindow.addButton("&Withdraw", 2, 4, new TAction() {
            @Override
            public void DO() {
                String amountStr = inputBox("Withdraw", "Enter amount:").getText();
                try {
                    double amt = Double.parseDouble(amountStr);
                    if (amt > 0) {
                        if (account.withdraw(amt)) {
                            messageBox("Success", "New balance: $" + account.getBalance()).show();
                        }
                    } else {
                        messageBox("Error", "Amount must be positive").show();
                    }
                } catch (OverDraftAmountException e) {
                    messageBox("Error", e.getMessage()).show();
                } catch (NumberFormatException e) {
                    messageBox("Error", "Invalid amount").show();
                } catch (Exception e) {
                    messageBox("Error", "Unexpected error: " + e.getMessage()).show();
                }
            }
        });
    }
}