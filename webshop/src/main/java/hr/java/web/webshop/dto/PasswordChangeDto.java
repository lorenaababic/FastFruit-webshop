package hr.java.web.webshop.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDto {

    @NotBlank(message = "Trenutna lozinka je obavezna")
    private String currentPassword;

    @NotBlank(message = "Nova lozinka je obavezna")
    @Size(min = 6, message = "Nova lozinka mora imati najmanje 6 znakova")
    private String newPassword;

    @NotBlank(message = "Potvrda lozinke je obavezna")
    private String confirmPassword;

}
