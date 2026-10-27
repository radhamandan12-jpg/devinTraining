package com.training.loan.web;

import com.training.loan.domain.AppUser;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.EmploymentStatus;
import com.training.loan.domain.enums.LoanPurpose;
import com.training.loan.service.DocumentService;
import com.training.loan.service.LoanApplicationService;
import com.training.loan.service.UserService;
import com.training.loan.web.dto.ApplicationForm;
import jakarta.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final LoanApplicationService applicationService;
    private final DocumentService documentService;
    private final UserService userService;

    public ApplicationController(LoanApplicationService applicationService,
                                 DocumentService documentService, UserService userService) {
        this.applicationService = applicationService;
        this.documentService = documentService;
        this.userService = userService;
    }

    @ModelAttribute("loanPurposes")
    public LoanPurpose[] loanPurposes() {
        return LoanPurpose.values();
    }

    @ModelAttribute("employmentStatuses")
    public EmploymentStatus[] employmentStatuses() {
        return EmploymentStatus.values();
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        AppUser user = currentUser(principal);
        model.addAttribute("applications", applicationService.forApplicant(user));
        return "applications/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("applicationForm", new ApplicationForm());
        return "applications/new";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute ApplicationForm applicationForm,
                         BindingResult result, Principal principal,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "applications/new";
        }
        LoanApplication app = applicationService.submit(currentUser(principal), applicationForm);
        redirect.addFlashAttribute("message",
                "Application submitted. Status: " + app.getStatus());
        return "redirect:/applications/" + app.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        LoanApplication app = applicationService.getForApplicant(id, currentUser(principal));
        model.addAttribute("app", app);
        model.addAttribute("communications", applicationService.communicationsFor(id));
        return "applications/detail";
    }

    @PostMapping("/{id}/documents")
    public String uploadDocument(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                 Principal principal, RedirectAttributes redirect) throws IOException {
        LoanApplication app = applicationService.getForApplicant(id, currentUser(principal));
        if (!file.isEmpty()) {
            documentService.attach(app, file);
            redirect.addFlashAttribute("message", "Document uploaded: " + file.getOriginalFilename());
        }
        return "redirect:/applications/" + id;
    }

    private AppUser currentUser(Principal principal) {
        return userService.findByUsername(principal.getName());
    }
}
