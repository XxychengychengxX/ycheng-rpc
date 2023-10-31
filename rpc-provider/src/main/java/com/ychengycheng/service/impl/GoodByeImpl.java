package com.ychengycheng.service.impl;

import com.ychengycheng.service.GoodByeService;
import com.ychengycheng.annotation.YchengApi;

/**
 * @author Valar Morghulis
 * @Date 2023/10/4
 */
@YchengApi
public class GoodByeImpl implements GoodByeService {
    @Override
    public String sayGoodBye() {
        return "Bye!!";
    }
}
