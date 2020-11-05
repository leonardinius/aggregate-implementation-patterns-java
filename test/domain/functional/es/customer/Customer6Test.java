package domain.oop.es.customer;

import domain.functional.es.customer.Customer6;
import domain.shared.command.ChangeCustomerEmailAddress;
import domain.shared.command.ChangeCustomerName;
import domain.shared.command.ConfirmCustomerEmailAddress;
import domain.shared.command.RegisterCustomer;
import domain.shared.event.*;
import domain.shared.value.EmailAddress;
import domain.shared.value.Hash;
import domain.shared.value.ID;
import domain.shared.value.PersonName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Customer6Test {
    private ID customerID;
    private EmailAddress emailAddress;
    private EmailAddress changedEmailAddress;
    private Hash confirmationHash;
    private Hash wrongConfirmationHash;
    private Hash changedConfirmationHash;
    private PersonName name;
    private PersonName changedName;

    @BeforeEach
    public void beforeEach() {
        customerID = ID.generate();
        emailAddress = EmailAddress.build("john@doe.com");
        changedEmailAddress = EmailAddress.build("john+changed@doe.com");
        confirmationHash = Hash.generate();
        wrongConfirmationHash = Hash.generate();
        changedConfirmationHash = Hash.generate();
        name = PersonName.build("John", "Doe");
        changedName = PersonName.build("Jayne", "Doe");
    }

    @Test
    public void registerCustomer() {
        // When RegisterCustomer
        var registerCustomer = RegisterCustomer.build(emailAddress.value, name.givenName, name.familyName);
        var customerRegistered = Customer6.register(registerCustomer);

        // Then CustomerRegistered
        assertNotNull(customerRegistered);

        //  and the payload should be as expected
        assertTrue(customerRegistered.customerID.equals(registerCustomer.customerID));
        assertTrue(customerRegistered.emailAddress.equals(registerCustomer.emailAddress));
        assertTrue(customerRegistered.confirmationHash.equals(registerCustomer.confirmationHash));
        assertTrue(customerRegistered.name.equals(registerCustomer.name));
    }

    @Test
    public void changeCustomerEmailAddress_withUnchangedEmailAddress() {
        // Given CustomerRegistered
        List<Event> events = List.of(CustomerRegistered.build(customerID, emailAddress, confirmationHash, name));

        // When ChangeCustomerEmailAddress
        var command = ChangeCustomerEmailAddress.build(customerID.value, emailAddress.value);
        var recordedEvents = Customer6.changeEmailAddress(events, command);

        // Then no event
        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void changeCustomerEmailAddress_whenItWasAlreadyChanged() {
        // Given CustomerRegistered
        //   and CustomerEmailAddressChanged
        var events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name),
                CustomerEmailAddressChanged.build(customerID, changedEmailAddress, changedConfirmationHash)
        );

        // When ChangeCustomerEmailAddress
        var command = ChangeCustomerEmailAddress.build(customerID.value, changedEmailAddress.value);
        var recordedEvents = Customer6.changeEmailAddress(events, command);

        // Then no event
        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void confirmCustomerEmailAddress_whenItWasPreviouslyConfirmedAndThenChanged() {
        // Given CustomerRegistered
        //   and CustomerEmailAddressConfirmed
        //   and CustomerEmailAddressChanged
        var events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name),
                CustomerEmailAddressConfirmed.build(customerID),
                CustomerEmailAddressChanged.build(customerID, changedEmailAddress, changedConfirmationHash)
        );

        // When ConfirmCustomerEmailAddress
        var command = ConfirmCustomerEmailAddress.build(customerID.value, changedConfirmationHash.value);
        var recordedEvents = Customer6.confirmEmailAddress(events, command);

        // Then CustomerEmailAddressConfirmed
        assertEquals(1, recordedEvents.size());
        assertEquals(CustomerEmailAddressConfirmed.class, recordedEvents.get(0).getClass());
        assertNotNull(recordedEvents.get(0).getClass());

        //  and the payload should be as expected
        CustomerEmailAddressConfirmed event = (CustomerEmailAddressConfirmed) recordedEvents.get(0);
        assertTrue(event.customerID.equals(command.customerID));

        // When the same command is handled again, Then no event should be recorded
        events = new ArrayList<Event>(events);
        events.add(event);
        recordedEvents = Customer6.confirmEmailAddress(events, command);
        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void confirmEmailAddress() {
        // Given CustomerRegistered
        List<Event> events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name)
        );

        // When ConfirmCustomerEmailAddress
        var command = ConfirmCustomerEmailAddress.build(customerID.value, confirmationHash.value);
        var recordedEvents = Customer6.confirmEmailAddress(events, command);

        // Then CustomerEmailAddressConfirmed
        assertEquals(1, recordedEvents.size());
        assertEquals(CustomerEmailAddressConfirmed.class, recordedEvents.get(0).getClass());
        assertNotNull(recordedEvents.get(0).getClass());

        //  and the payload should be as expected
        var event = (CustomerEmailAddressConfirmed) recordedEvents.get(0);
        assertTrue(event.customerID.equals(command.customerID));

        // When the same command is handled again, Then no event should be recorded
        events = new ArrayList<Event>(events);
        events.add(event);
        recordedEvents = Customer6.confirmEmailAddress(events, command);
        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void confirmEmailAddress_withWrongConfirmationHash() {
        // Given CustomerRegistered
        List<Event> events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name)
        );

        // When ConfirmCustomerEmailAddress (with wrong confirmationHash)
        var command = ConfirmCustomerEmailAddress.build(customerID.value, wrongConfirmationHash.value);
        var recordedEvents = Customer6.confirmEmailAddress(events, command);

        // Then CustomerEmailAddressConfirmationFailed
        assertEquals(1, recordedEvents.size());
        assertEquals(CustomerEmailAddressConfirmationFailed.class, recordedEvents.get(0).getClass());
        assertNotNull(recordedEvents.get(0).getClass());

        //  and the payload should be as expected
        CustomerEmailAddressConfirmationFailed event = (CustomerEmailAddressConfirmationFailed) recordedEvents.get(0);
        assertTrue(event.customerID.equals(command.customerID));
    }

    @Test
    public void confirmEmailAddress_whenItWasAlreadyConfirmed() {
        // Given CustomerRegistered
        //   and CustomerEmailAddressConfirmed
        var events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name),
                CustomerEmailAddressConfirmed.build(customerID)
        );

        // When ConfirmCustomerEmailAddress
        var command = ConfirmCustomerEmailAddress.build(customerID.value, confirmationHash.value);
        var recordedEvents = Customer6.confirmEmailAddress(events, command);

        // Then no event
        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void confirmEmailAddress_withWrongConfirmationHash_whenItWasAlreadyConfirmed() {
        // Given CustomerRegistered
        //   and CustomerEmailAddressConfirmed
        var events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name),
                CustomerEmailAddressConfirmed.build(customerID)
        );

        // When ConfirmCustomerEmailAddress (with wrong confirmationHash)
        var command = ConfirmCustomerEmailAddress.build(customerID.value, wrongConfirmationHash.value);
        var recordedEvents = Customer6.confirmEmailAddress(events, command);

        // Then CustomerEmailAddressConfirmationFailed
        assertEquals(1, recordedEvents.size());
        assertEquals(CustomerEmailAddressConfirmationFailed.class, recordedEvents.get(0).getClass());
        assertNotNull(recordedEvents.get(0).getClass());

        //  and the payload should be as expected
        CustomerEmailAddressConfirmationFailed event = (CustomerEmailAddressConfirmationFailed) recordedEvents.get(0);
        assertTrue(event.customerID.equals(command.customerID));
    }

    @Test
    public void changeCustomerEmailAddress() {
        // Given CustomerRegistered
        List<Event> events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name)
        );

        // When ChangeCustomerEmailAddress
        var command = ChangeCustomerEmailAddress.build(customerID.value, changedEmailAddress.value);
        var recordedEvents = Customer6.changeEmailAddress(events, command);

        // Then CustomerEmailAddressChanged
        assertEquals(1, recordedEvents.size());
        assertEquals(CustomerEmailAddressChanged.class, recordedEvents.get(0).getClass());
        assertNotNull(recordedEvents.get(0).getClass());

        //  and the payload should be as expected
        CustomerEmailAddressChanged event = (CustomerEmailAddressChanged) recordedEvents.get(0);
        assertTrue(event.customerID.equals(command.customerID));
        assertTrue(event.emailAddress.equals(command.emailAddress));
        assertTrue(event.confirmationHash.equals(command.confirmationHash));

        // When the same command is handled again, Then no event should be recorded
        events = new ArrayList<Event>(events);
        events.add(event);
        recordedEvents = Customer6.changeEmailAddress(events, command);
        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void changeCustomerName() {
        // Given CustomerRegistered
        List<Event> events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name)
        );

        // When ChangeCustomerName
        var command = ChangeCustomerName.build(customerID.value, changedName.givenName, changedName.familyName);
        var recordedEvents = Customer6.changeName(events, command);

        // Then CustomerNameChanged
        assertEquals(1, recordedEvents.size());
        assertEquals(CustomerNameChanged.class, recordedEvents.get(0).getClass());
        assertNotNull(recordedEvents.get(0).getClass());

        //  and the payload should be as expected
        var event = (CustomerNameChanged) recordedEvents.get(0);
        assertTrue(event.customerID.equals(command.customerID));
        assertTrue(event.name.equals(command.name));

        // When the same command is handled again, Then no event should be recorded
        events = new ArrayList<Event>(events);
        events.add(event);
        recordedEvents = Customer6.changeName(events, command);
        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void changeCustomerName_withUnchangedName() {
        // Given CustomerRegistered
        List<Event> events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name)
        );

        // When ChangeCustomerName
        var command = ChangeCustomerName.build(customerID.value, name.givenName, name.familyName);
        var recordedEvents = Customer6.changeName(events, command);

        // Then no event
        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void changeCustomerName_whenItWasAlreadyChanged() {
        // Given CustomerRegistered
        //   and CustomerNameChanged
        var events = List.of(
                CustomerRegistered.build(customerID, emailAddress, confirmationHash, name),
                CustomerNameChanged.build(customerID, changedName)
        );

        // When ChangeCustomerName
        var command = ChangeCustomerName.build(customerID.value, changedName.givenName, changedName.familyName);
        var recordedEvents = Customer6.changeName(events, command);

        // Then no event
        assertEquals(0, recordedEvents.size());
    }
}