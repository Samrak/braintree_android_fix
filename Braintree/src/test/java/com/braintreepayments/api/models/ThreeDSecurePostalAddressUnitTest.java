package com.braintreepayments.api.models;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ThreeDSecurePostalAddressUnitTest {

    @Test
    public void constructsCorrectly() throws JSONException {
        ThreeDSecurePostalAddress postalAddress = new ThreeDSecurePostalAddress()
                .streetAddress("123 Fake St.")
                .extendedAddress("Apt. 3")
                .locality("Oakland")
                .region("CA")
                .postalCode("94602")
                .countryCodeAlpha2("US")
                .firstName("John")
                .lastName("Fakerson")
                .phoneNumber("5151231234");

        assertEquals("123 Fake St.", postalAddress.getStreetAddress());
        assertEquals("Apt. 3", postalAddress.getExtendedAddress());
        assertEquals("Oakland", postalAddress.getLocality());
        assertEquals("CA", postalAddress.getRegion());
        assertEquals("94602", postalAddress.getPostalCode());
        assertEquals("US", postalAddress.getCountryCodeAlpha2());
        assertEquals("John", postalAddress.getFirstName());
        assertEquals("Fakerson", postalAddress.getLastName());
        assertEquals("5151231234", postalAddress.getPhoneNumber());
    }

    @Test
    public void testWriteToParcel_serializesCorrectly() throws JSONException {
        ThreeDSecurePostalAddress preSerialized = new ThreeDSecurePostalAddress()
                .streetAddress("123 Fake St.")
                .extendedAddress("Apt. 3")
                .locality("Oakland")
                .region("CA")
                .postalCode("94602")
                .countryCodeAlpha2("US")
                .firstName("John")
                .lastName("Fakerson")
                .phoneNumber("5151231234");

        Parcel parcel = Parcel.obtain();
        preSerialized.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        ThreeDSecurePostalAddress postSerialized = ThreeDSecurePostalAddress.CREATOR.createFromParcel(parcel);

        assertNotNull(postSerialized);
        assertEquals("123 Fake St.", postSerialized.getStreetAddress());
        assertEquals("Apt. 3", postSerialized.getExtendedAddress());
        assertEquals("Oakland", postSerialized.getLocality());
        assertEquals("CA", postSerialized.getRegion());
        assertEquals("94602", postSerialized.getPostalCode());
        assertEquals("US", postSerialized.getCountryCodeAlpha2());
        assertEquals("John", postSerialized.getFirstName());
        assertEquals("Fakerson", postSerialized.getLastName());
        assertEquals("5151231234", postSerialized.getPhoneNumber());
    }

    @Test
    public void testToJson_buildsAllParameters() throws JSONException{
        ThreeDSecurePostalAddress address = new ThreeDSecurePostalAddress()
                .streetAddress("123 Fake St.")
                .extendedAddress("Apt. 3")
                .locality("Oakland")
                .region("CA")
                .postalCode("94602")
                .countryCodeAlpha2("US")
                .firstName("John")
                .lastName("Fakerson")
                .phoneNumber("5151231234");

        JSONObject jsonParams = address.toJson();
        JSONObject jsonBillingAddress = jsonParams.getJSONObject("billingAddress");

        assertEquals("John", jsonParams.get("firstName"));
        assertEquals("Fakerson", jsonParams.get("lastName"));
        assertEquals("5151231234", jsonParams.get("phoneNumber"));
        assertEquals("123 Fake St.", jsonBillingAddress.get("line1"));
        assertEquals("Apt. 3", jsonBillingAddress.get("line2"));
        assertEquals("Oakland", jsonBillingAddress.get("city"));
        assertEquals("CA", jsonBillingAddress.get("state"));
        assertEquals("94602", jsonBillingAddress.get("postalCode"));
        assertEquals("US", jsonBillingAddress.get("countryCode"));
    }

    @Test
    public void testToJson_buildsPartialParameters() throws JSONException{
        ThreeDSecurePostalAddress address = new ThreeDSecurePostalAddress()
                .streetAddress("123 Fake St.")
                .extendedAddress("Apt. 3")
                .locality("Oakland")
                .region("CA")
                .postalCode("94602")
                .firstName("John")
                .lastName("Fakerson");

        JSONObject jsonParams = address.toJson();
        JSONObject jsonBillingAddress = jsonParams.getJSONObject("billingAddress");

        assertEquals("John", jsonParams.get("firstName"));
        assertEquals("Fakerson", jsonParams.get("lastName"));
        assertTrue(jsonParams.isNull("phoneNumber"));
        assertEquals("123 Fake St.", jsonBillingAddress.get("line1"));
        assertEquals("Apt. 3", jsonBillingAddress.get("line2"));
        assertEquals("Oakland", jsonBillingAddress.get("city"));
        assertEquals("CA", jsonBillingAddress.get("state"));
        assertEquals("94602", jsonBillingAddress.get("postalCode"));
        assertTrue(jsonBillingAddress.isNull("countryCode"));
    }

    @Test
    public void testToJson_buildsEmptyParameters() throws JSONException{
        ThreeDSecurePostalAddress address = new ThreeDSecurePostalAddress();

        JSONObject jsonParams = address.toJson();

        assertTrue(jsonParams.isNull("billingAddress"));
        assertTrue(jsonParams.isNull("firstName"));
        assertTrue(jsonParams.isNull("lastName"));
        assertTrue(jsonParams.isNull("phoneNumber"));
    }
}
