package com.braintreepayments.api;

import org.junit.Test;

import static com.samsung.android.sdk.samsungpay.v2.SpaySdk.SPAY_NOT_READY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SamsungPayClientUnitTest {

    @Test
    public void goToUpdatePage_forwardsInvocationToInternalClient() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        SamsungPayClient sut = new SamsungPayClient(braintreeClient);

        SamsungPayInternalClient internalClient = mock(SamsungPayInternalClient.class);
        sut.internalClient = internalClient;

        sut.goToUpdatePage();
        verify(internalClient).goToSamsungPayUpdatePage();
    }

    @Test
    public void activateSamsungPay_forwardsInvocationToInternalClient() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        SamsungPayClient sut = new SamsungPayClient(braintreeClient);

        SamsungPayInternalClient internalClient = mock(SamsungPayInternalClient.class);
        sut.internalClient = internalClient;

        sut.activateSamsungPay();
        verify(internalClient).activateSamsungPay();
    }

    @Test
    public void isReadyToPay_whenSamsungPayNotReady_callsBackFalse() {
        BraintreeClient braintreeClient = new MockBraintreeClientBuilder().build();
        SamsungPayClient sut = new SamsungPayClient(braintreeClient);

        sut.internalClient = new MockSamsungPayInternalClientBuilder()
                .getSamsungPayStatusSuccess(SPAY_NOT_READY)
                .build();

        SamsungIsReadyToPayCallback callback = mock(SamsungIsReadyToPayCallback.class);
        sut.isReadyToPay(callback);

        verify(callback).onResult(false, null);
    }

    @Test
    public void isReadyToPay_whenSamsungPayNotSupported_callsBackFalse() {

    }

    @Test
    public void isReadyToPay_whenSamsungPayReady_andAcceptedCardsExist_callsBackTrue() {

    }

    @Test
    public void isReadyToPay_whenSamsungPayReady_andNoBraintreeAcceptedCardsExist_callsBackFalse() {

    }

    @Test
    public void isReadyToPay_whenSamsungPayReady_andNoSamsungPayAcceptedCardsExist_callsBackFalse() {

    }
}