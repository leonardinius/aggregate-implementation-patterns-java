package domain.functional.es.customer;

import domain.shared.command.ChangeCustomerEmailAddress;
import domain.shared.command.ConfirmCustomerEmailAddress;
import domain.shared.command.RegisterCustomer;
import domain.shared.event.*;
import domain.shared.value.EmailAddress;
import domain.shared.value.Hash;

import java.util.List;

public class Customer5 {
    public static CustomerRegistered register(RegisterCustomer command) {
        return CustomerRegistered.build(
                command.customerID,
                command.emailAddress,
                command.confirmationHash,
                command.name
        );
    }

    public static List<Event> confirmEmailAddress(List<Event> eventStream, ConfirmCustomerEmailAddress command) {
        boolean isEmailAddressConfirmed = false;
        Hash confirmationHash = null;
        for (Event event : eventStream) {
            if (event instanceof CustomerRegistered) {
                confirmationHash = ((CustomerRegistered) event).confirmationHash;
            } else if (event instanceof CustomerEmailAddressConfirmed) {
                isEmailAddressConfirmed = true;
            } else if (event instanceof CustomerEmailAddressChanged) {
                isEmailAddressConfirmed = false;
                confirmationHash = ((CustomerEmailAddressChanged) event).confirmationHash;
            }
        }

        if (confirmationHash.equals(command.confirmationHash)) {
            return isEmailAddressConfirmed
                    ? List.of()
                    : List.of(CustomerEmailAddressConfirmed.build(command.customerID));
        }

        return List.of(CustomerEmailAddressConfirmationFailed.build(command.customerID));
    }

    public static List<Event> changeEmailAddress(List<Event> eventStream, ChangeCustomerEmailAddress command) {
        EmailAddress emailAddress = null;
        for (Event event : eventStream) {
            if (event instanceof CustomerRegistered) {
                emailAddress = ((CustomerRegistered) event).emailAddress;
            } else if (event instanceof CustomerEmailAddressChanged) {
                emailAddress = ((CustomerEmailAddressChanged) event).emailAddress;
            }
        }

        if(command.emailAddress.equals(emailAddress)){
            return List.of();
        }

        return List.of(CustomerEmailAddressChanged.build(
                command.customerID,
                command.emailAddress,
                command.confirmationHash
        ));
    }
}
