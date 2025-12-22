package com.project.evgo.user;

import com.project.evgo.user.internal.StationOwnerProfile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PdfParsingService {
    StationOwnerProfile parseRegistrationPdf(MultipartFile file);
}
