package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import com.project.evgo.sharedkernel.enums.StationOwnerType;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.PdfParsingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PdfParsingServiceImpl implements PdfParsingService {

    private static final String PDF_KEY_OWNER_TYPE_RADIO = "radio_group_1kyib";
    private static final String PDF_VAL_TYPE_ENTERPRISE = "Value_fecy";

    private static final String PDF_KEY_EMAIL = "text_3qgxw";
    private static final String PDF_KEY_PHONE = "text_4fdea";

    // Enterprise Fields
    private static final String PDF_KEY_BUSINESS_NAME = "text_5brvf";
    private static final String PDF_KEY_TAX_CODE = "text_6otuv";

    // Individual Fields
    private static final String PDF_KEY_FULL_NAME = "text_7aclf";
    private static final String PDF_KEY_ID_NUMBER = "text_8dyha";

    // Bank Fields
    private static final String PDF_KEY_BANK_ACCOUNT = "text_9eifn";
    private static final String PDF_KEY_BANK_NAME = "text_10epel";

    @Override
    public StationOwnerProfile parseRegistrationPdf(MultipartFile file) {
        validatePdfFile(file);
        Map<String, String> formData = extractFormFields(file);
        return mapToStationOwnerProfile(formData);
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.PDF_PARSING_FAILED, "PDF file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Only PDF files are allowed");
        }
    }

    private Map<String, String> extractFormFields(MultipartFile file) {
        Map<String, String> formData = new HashMap<>();

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            if (acroForm == null) {
                log.warn("PDF file does not contain form fields");
                return formData;
            }

            for (PDField field : acroForm.getFields()) {
                String fieldName = field.getFullyQualifiedName();
                String fieldValue = field.getValueAsString();

                if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                    formData.put(fieldName, fieldValue.trim());
                }
            }

            log.info("Successfully extracted {} form fields from PDF", formData.size());
            return formData;

        } catch (Exception e) {
            log.error("Failed to parse PDF file: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.PDF_PARSING_FAILED);
        }
    }

    private StationOwnerProfile mapToStationOwnerProfile(Map<String, String> formData) {
        try {
            StationOwnerProfile profile = new StationOwnerProfile();

            if (formData.isEmpty()) {
                throw new AppException(ErrorCode.PDF_PARSING_FAILED, "No form data found in PDF");
            }

            if (!formData.containsKey(PDF_KEY_OWNER_TYPE_RADIO)) {
                throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Owner type information is missing in PDF");
            }

            String ownerTypeVal = formData.get(PDF_KEY_OWNER_TYPE_RADIO);

            boolean isEnterprise = PDF_VAL_TYPE_ENTERPRISE.equals(ownerTypeVal);

            StationOwnerType ownerType = isEnterprise ? StationOwnerType.ENTERPRISE : StationOwnerType.INDIVIDUAL;
            profile.setOwnerType(ownerType);


            // 2. Get common fields
            String email = formData.get(PDF_KEY_EMAIL);
            profile.setContactEmail(email);

            String phone = formData.get(PDF_KEY_PHONE);
            profile.setContactPhone(phone);

            String bankAccount = formData.get(PDF_KEY_BANK_ACCOUNT);
            profile.setBankAccount(bankAccount);

            String bankName = formData.get(PDF_KEY_BANK_NAME);
            profile.setBankName(bankName);

            if (ownerType == StationOwnerType.ENTERPRISE) {
                profile.setBusinessName(formData.get(PDF_KEY_BUSINESS_NAME));
                profile.setTaxCode(formData.get(PDF_KEY_TAX_CODE));
            } else {
                profile.setFullName(formData.get(PDF_KEY_FULL_NAME));
                profile.setIdNumber(formData.get(PDF_KEY_ID_NUMBER));
            }

            if (email == null || phone == null) {
                throw new AppException(ErrorCode.EMAIL_OR_PHONE_REQUIRED);
            }

            profile.setStatus(StationOwnerStatus.SUBMITTED);

            validateRequiredFields(profile);

            return profile;

        } catch (IllegalArgumentException e) {
            log.error("Invalid owner type in PDF: {}", e.getMessage());
            throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Invalid owner type in PDF file");
        }
    }

    private void validateRequiredFields(StationOwnerProfile profile) {
        if (profile.getContactEmail() == null || profile.getContactEmail().trim().isEmpty()) {
            throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Email is required in PDF");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!profile.getContactEmail().matches(emailRegex)) {
            throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Invalid email format in PDF");
        }

        if (profile.getContactPhone() == null || profile.getContactPhone().trim().isEmpty()) {
            throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Phone is required in PDF");
        }

        String phoneRegex = "^(\\+?84|0)[0-9]{9,10}$";
        if (!profile.getContactPhone().matches(phoneRegex)) {
            throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Invalid phone format in PDF");
        }

        if (profile.getOwnerType() == StationOwnerType.ENTERPRISE) {
            if (profile.getBusinessName() == null || profile.getBusinessName().trim().isEmpty()) {
                throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Business name is required for enterprise owner");
            }
            if (profile.getTaxCode() == null || profile.getTaxCode().trim().isEmpty()) {
                throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Tax code is required for enterprise owner");
            }
        } else {
            if (profile.getFullName() == null || profile.getFullName().trim().isEmpty()) {
                throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Full name is required for individual owner");
            }
            if (profile.getIdNumber() == null || profile.getIdNumber().trim().isEmpty()) {
                throw new AppException(ErrorCode.PDF_PARSING_FAILED, "Identity number is required for individual owner");
            }
        }
    }
}
