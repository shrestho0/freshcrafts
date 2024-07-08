package fresh.crafts.engine.v1.controllers;

import fresh.crafts.engine.v1.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.services.TokensService;

@RestController
@RequestMapping(value = "/api/v1/tokens", consumes = "application/json", produces = "application/json")

public class TokensController {

    @Autowired
    private TokensService tokensService;


    @GetMapping("/allowed-providers")
    public ProvidersResponseDto authProviders() {
        ProvidersResponseDto response = new ProvidersResponseDto(false, null);

        response.setSuccess(true);
        response.setProviders(tokensService.getEnabledOAuthProviders());

        System.err.println("[DEBUG] TokensController - authProviders: " + response);

        return response;

    }

    @PostMapping("/generate")
    public GenerateTokenResponseDto getTokens(@RequestBody GenerateTokenRequestDto tokensDto) {

        GenerateTokenResponseDto res = new GenerateTokenResponseDto();
        try {
            res = tokensService.generateToken(tokensDto);
        } catch (Exception e) {
            res.setMessage(e.getMessage());
        }
        // try {
        // return tokensService.generateToken(tokensDto);
        // } catch (Exception e) {
        // GenerateTokenResponseDto res = new GenerateTokenResponseDto();
        // res.setMessage(e.getMessage());
        // return res;
        // }
        return res;
    }

    @PostMapping("/refresh")
    public GenerateRefreshTokenResponseDto refreshTokens(@RequestBody GenerateRefreshTokenRequestDto tokensDto) {

        GenerateRefreshTokenResponseDto res = new GenerateRefreshTokenResponseDto();

        try {

            res = tokensService.refreshToken(tokensDto);
        } catch (Exception e) {
            res.setMessage(e.getMessage());
        }
        return res;
    }

    @PostMapping("/invalidate")
    public CommonResponseDto invalidateToken(@RequestBody InvalidateTokenRequestDto token){

        CommonResponseDto res = new CommonResponseDto();
        res.setSuccess(false);

        try {
            res = tokensService.invalidateToken(token);
        } catch (Exception e) {
            res.setMessage(e.getMessage());
        }

        return res;
    }

}
