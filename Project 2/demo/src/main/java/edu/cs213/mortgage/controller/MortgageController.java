package edu.cs213.mortgage.controller;

import edu.cs213.mortgage.model.MortgageApi.*;
import edu.cs213.mortgage.service.MortgageApiService;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mortgages")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class MortgageController {
    private final MortgageApiService service;
    public MortgageController(MortgageApiService service) { this.service = service; }
    @GetMapping("/options")
    public Options options() throws SQLException { return service.options(); }
    @PostMapping("/search")
    public SearchResponse search(@RequestBody SearchRequest request) throws SQLException { return service.search(request); }
    @PostMapping("/rate")
    public RateResponse rate(@RequestBody SearchRequest request) throws SQLException { return service.rate(request); }
    @PostMapping("/package")
    public PackageResponse packageMortgages(@RequestBody PackageRequest request) throws SQLException {
        return service.packageMortgages(request);
    }
}
