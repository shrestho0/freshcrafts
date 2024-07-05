package fresh.crafts.engine.v1.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.GenerateTokenRequestDto;
import fresh.crafts.engine.v1.dtos.GenerateTokenResponseDto;
import fresh.crafts.engine.v1.dtos.ProvidersResponseDto;
import fresh.crafts.engine.v1.types.Tokens;
import fresh.crafts.engine.v1.types.enums.OAuthProviderEnum;
import fresh.crafts.engine.v1.utils.TokenUtil;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping(value="/api/v1/tokens", consumes = "application/json", produces="application/json")
public class TokensController {



    @GetMapping("allowed-providers")
    public ProvidersResponseDto authProviders(){
        List<OAuthProviderEnum> providers = new ArrayList<>();


        // TODO: Check from mongo and set necessary one
        providers.add(OAuthProviderEnum.OAUTH_GITHUB);
        providers.add(OAuthProviderEnum.OAUTH_GOOGLE);


        return new ProvidersResponseDto(true,providers);
        // this will check from mongo and return if available login methods

    }



    @PostMapping("generate")
    public GenerateTokenResponseDto generateToken(@RequestBody @Validated GenerateTokenRequestDto tokenDto) {

        // take email, passsword or verified oauth email from cockpit

        System.out.println("[DEBUG] TokensController - tokenDto: "+tokenDto);



        // TODO:  get system user data;
        Object payload = null;
        
        try {
            Tokens tokens = TokenUtil.generate(payload);
            return  new GenerateTokenResponseDto(true,tokens, tokenDto.getLogin_type());
        } catch (Exception e) {
            return new GenerateTokenResponseDto(false, null, tokenDto.getLogin_type());
        }


    }


}
