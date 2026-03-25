package bookonline.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {
	@Autowired private Cloudinary cloudinary;
	
	//upload ảnh
    public String uploadImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File tải lên không phải là định dạng ảnh!");
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
            ObjectUtils.asMap(
                "resource_type", "image",
                "allowed_formats", new String[] {"jpg", "jpeg", "png", "gif", "webp"}
            ));
            
        return uploadResult.get("url").toString();
    }

    //upload file pdf
    public String uploadPdf(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(MediaType.APPLICATION_PDF_VALUE)) {
            throw new IllegalArgumentException("Tài liệu tải lên bắt buộc phải là file PDF!");
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
            ObjectUtils.asMap(
                "resource_type", "raw", 
                "allowed_formats", new String[] {"pdf"}
            ));
            
        return uploadResult.get("url").toString();
    }
}
