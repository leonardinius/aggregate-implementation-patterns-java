package domain.functional.es.customer;

import domain.shared.event.CustomerEmailAddressChanged;
import domain.shared.event.CustomerEmailAddressConfirmed;
import domain.shared.event.CustomerRegistered;
import domain.shared.event.Event;
import domain.shared.value.EmailAddress;
import domain.shared.value.Hash;
import domain.shared.value.PersonName;

import java.util.List;

public class CustomerState {
    EmailAddress emailAddress;
    Hash confirmationHash;
    PersonName name;
    boolean isEmailAddressConfirmed;

    private CustomerState() {
    }

    public static CustomerState reconstitute(List<Event> events) {
        var customer = new CustomerState();

        customer.apply(events);

        return customer;
    }

    void apply(List<Event> events) {
        for (Event event : events) {
            if (event instanceof CustomerRegistered) {
                CustomerRegistered customerRegistered = (CustomerRegistered) event;
                this.emailAddress = customerRegistered.emailAddress;
                this.confirmationHash = customerRegistered.confirmationHash;
                this.name = customerRegistered.name;
                this.isEmailAddressConfirmed = false;
                continue;
            }
            if (event instanceof CustomerEmailAddressConfirmed) {
                this.isEmailAddressConfirmed = true;
                continue;
            }
            if (event instanceof CustomerEmailAddressChanged) {
                CustomerEmailAddressChanged emailAddressChanged = (CustomerEmailAddressChanged) event;
                this.emailAddress = emailAddressChanged.emailAddress;
                this.confirmationHash = emailAddressChanged.confirmationHash;
                this.isEmailAddressConfirmed = false;
                continue;
            }
        }
    }
}
