package com.example.weathertrip_sep490.model;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PartnerRequestBody {
    private RequestBody businessName;
    private RequestBody businessAddress;
    private RequestBody businessPhone;
    private RequestBody businessEmail;
    private MultipartBody.Part businessLicenseFile;

    public PartnerRequestBody(RequestBody businessName,
                              RequestBody businessAddress,
                              RequestBody businessPhone,
                              RequestBody businessEmail,
                              MultipartBody.Part businessLicenseFile) {
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.businessPhone = businessPhone;
        this.businessEmail = businessEmail;
        this.businessLicenseFile = businessLicenseFile;
    }

    public RequestBody getBusinessName() { return businessName; }
    public RequestBody getBusinessAddress() { return businessAddress; }
    public RequestBody getBusinessPhone() { return businessPhone; }
    public RequestBody getBusinessEmail() { return businessEmail; }
    public MultipartBody.Part getBusinessLicenseFile() { return businessLicenseFile; }
}
