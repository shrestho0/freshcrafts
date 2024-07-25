package fresh.crafts.engine.v1.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.repositories.TestTestRepository;

@Service
public class TestTestService {

    @Autowired
    TestTestRepository testRepository;

    public HashMap<String, String> test1(HashMap<String, String> res) {

        return res;
    }

}
