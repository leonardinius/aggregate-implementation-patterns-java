package domain.oop.es.customer;

import domain.shared.command.ChangeCustomerEmailAddress;
import domain.shared.command.ConfirmCustomerEmailAddress;
import domain.shared.command.RegisterCustomer;
import domain.shared.event.*;
import domain.shared.value.EmailAddress;
import domain.shared.value.Hash;
import domain.shared.value.PersonName;

import java.util.ArrayList;
import java.util.List;

public final class Customer4 {
    private EmailAddress emailAddress;
    private Hash confirmationHash;
    private boolean isEmailAddressConfirmed;
    private PersonName name;

    private final List<Event> recordedEvents;

    private Customer4() {
        recordedEvents = new ArrayList<>();
    }

    public static Customer4 register(RegisterCustomer command) {
        Customer4 customer = new Customer4();

        customer.record(CustomerRegistered.build(
                command.customerID,
                command.emailAddress,
                command.confirmationHash,
                command.name
        ));

        return customer;
    }

    private void record(Event... events) {
        recordedEvents.addAll(List.of(events));
    }

    public static Customer4 reconstitute(List<Event> events) {
        var customer = new Customer4();

        customer.apply(events);

        return customer;
    }

    public void confirmEmailAddress(ConfirmCustomerEmailAddress command) {
        if (command.confirmationHash.equals(confirmationHash)) {
            if (isEmailAddressConfirmed) {
                return;
            }

            record(CustomerEmailAddressConfirmed.build(
                    command.customerID
            ));
        } else {
            record(CustomerEmailAddressConfirmationFailed.build(
                    command.customerID
            ));
        }
    }

    public void changeEmailAddress(ChangeCustomerEmailAddress command) {
        if(command.emailAddress.equals(emailAddress)){
            return;
        }
        record(CustomerEmailAddressChanged.build(
                command.customerID,
                command.emailAddress,
                command.confirmationHash
        ));
    }

    public List<Event> getRecordedEvents() {
        return recordedEvents;
    }

    private void recordThat(Event event) {
        recordedEvents.add(event);
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
