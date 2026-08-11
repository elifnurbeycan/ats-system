package com.yasarbilgi.ats.common.contract;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/application-contract")
public class ApplicationContractController {
    @GetMapping
    public ApplicationContractResponse getContract() { return ApplicationContractResponse.current(); }
}
