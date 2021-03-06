/*
 * Copyright 2018-2019 zTianzeng Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ztianzeng.agouti.core.parse;

import com.ztianzeng.agouti.core.resource.AbstractResource;
import com.ztianzeng.agouti.core.resource.ClassPathResource;
import com.ztianzeng.common.workflow.WorkFlowDef;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author zhaotianzeng
 * @version V1.0
 * @date 2019-04-16 19:53
 */
@Slf4j
public class WorkFlowParseTest {

    @Test
    public void parse() {
        String path = "agouti/workFlowDef.json";
        AbstractResource resource = new ClassPathResource(
                path, ClassLoader.getSystemClassLoader());

        WorkFlowDef workFlowDef = WorkFlowParse.parse(resource);

        log.info(workFlowDef.toString());
    }
}