package domain.oop.es.customer;

import domain.shared.command.ChangeCustomerEmailAddress;
import domain.shared.command.ConfirmCustomerEmailAddress;
import domain.shared.command.RegisterCustomer;
import domain.shared.event.*;
import domain.shared.value.EmailAddress;
import domain.shared.value.Hash;
import domain.shared.value.PersonName;

import java.util.List;

public final class Customer3 {
    private EmailAddress emailAddress;
    private Hash confirmationHash;
    private boolean isEmailAddressConfirmed;
    private PersonName name;

    private Customer3() {
    }

    public static CustomerRegistered register(RegisterCustomer command) {
        return CustomerRegistered.build(command.customerID, command.emailAddress, command.confirmationHash, command.name);
    }

    public static Customer3 reconstitute(List<Event> events) {
        Customer3 customer = new Customer3();

        customer.apply(events);

        return customer;
    }

    public List<Event> confirmEmailAddress(ConfirmCustomerEmailAddress command) {
        Event event;
        if (command.confirmationHash.equals(confirmationHash)) {
            if (this.isEmailAddressConfirmed) {
                return List.of();
            }
            event = CustomerEmailAddressConfirmed.build(command.customerID);
        } else {
            event = CustomerEmailAddressConfirmationFailed.build(command.customerID);
        }

        return List.of(event);
    }

    public List<Event> changeEmailAddress(ChangeCustomerEmailAddress command) {
        if(command.emailAddress.equals(emailAddress)){
            return List.of();
        }

        return List.of(CustomerEmailAddressChanged.build(
                command.customerID,
                command.emailAddress,
                command.confirmationHash)
        );
    }

    void apply(List<Event> events) {
        for (Event event : events) {
            apply(event);
        }
    }

    void apply(Event event) {
        if (event instanceof CustomerRegistered) {
            CustomerRegistered customerRegistered = (CustomerRegistered) event;
            this.emailAddress = customerRegistered.emailAddress;
            this.confirmationHash = customerRegistered.confirmationHash;
            this.name = customerRegistered.name;
            this.isEmailAddressConfirmed = false;
        } else if (event instanceof CustomerEmailAddressConfirmed) {
            this.isEmailAddressConfirmed = true;
        } else if (event instanceof CustomerEmailAddressChanged) {
            CustomerEmailAddressChanged emailAddressChanged = (CustomerEmailAddressChanged) event;
            this.emailAddress = emailAddressChanged.emailAddress;
            this.confirmationHash = emailAddressChanged.confirmationHash;
            this.isEmailAddressConfirmed = false;
        }
    }
}
