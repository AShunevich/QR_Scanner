package com.ashunevich.qrscanner;

public interface QRCodeFoundListener {
    void onQRCodeFound(String qrCode);
    void qrCodeNotFound();
}
