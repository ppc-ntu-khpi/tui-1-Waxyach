import com.mybank.domain.Bank;
import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

public class TUIDemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUSTOMER_INFO = 2010;

    public static void main(String[] args) throws Exception {
        TUIDemo demo = new TUIDemo();
        (new Thread(demo)).start();
    }

    public TUIDemo() throws Exception {
        super(BackendType.SWING);

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
                    details.setText("Owner Name: John Doe (id=" + num + ")\nAccount Type: 'Checking'\nAccount Balance: $200.00");
                } catch (Exception e) {
                    messageBox("Error", "You must provide a valid customer number!").show();
                }
            }
        });
    }
}
