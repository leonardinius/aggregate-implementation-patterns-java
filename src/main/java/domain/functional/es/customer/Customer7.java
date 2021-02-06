package domain.functional.es.customer;

import domain.shared.command.ChangeCustomerEmailAddress;
import domain.shared.command.ConfirmCustomerEmailAddress;
import domain.shared.command.RegisterCustomer;
import domain.shared.event.*;

import java.util.List;

public class Customer7 {
    public static CustomerRegistered register(RegisterCustomer command) {
        return CustomerRegistered.build(
                command.customerID,
                command.emailAddress,
                command.confirmationHash,
                command.name
        );
    }

    public static List<Event> confirmEmailAddress(CustomerState current, ConfirmCustomerEmailAddress command) {
        Event event;
        if (command.confirmationHash.equals(current.confirmationHash)) {
            if (current.isEmailAddressConfirmed) {
                return List.of();
            }
            event = CustomerEmailAddressConfirmed.build(command.customerID);
        } else {
            event = CustomerEmailAddressConfirmationFailed.build(command.customerID);
        }

        return List.of(event);
    }

    public static List<Event> changeEmailAddress(CustomerState current, ChangeCustomerEmailAddress command) {
        if(command.emailAddress.equals(current.emailAddress)){
            return List.of();
        }

        return List.of(CustomerEmailAddressChanged.build(
                command.customerID,
                command.emailAddress,
                command.confirmationHash)
        );
    }
}
