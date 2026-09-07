package com.ir.formgenerator.controller;

import com.ir.formgenerator.dto.GenerationResultDto;
import com.ir.formgenerator.model.GenerationResult;
import com.ir.formgenerator.service.FormGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/forms")
public class FormGenerationController {

    private final FormGenerationService formGenerationService;

    public FormGenerationController(FormGenerationService formGenerationService) {
        this.formGenerationService = formGenerationService;
    }


    @PostMapping("/generate")
    public ResponseEntity<GenerationResultDto> generate(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        GenerationResult result = formGenerationService.generate(username);
        return ResponseEntity.ok(new GenerationResultDto(result));
    }
}
