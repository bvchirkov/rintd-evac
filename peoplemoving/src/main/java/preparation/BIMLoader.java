/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * BIMLoader is part of jSimulationMoving.
 *
 * jSimulationMoving is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSimulationMoving is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSimulationMoving. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------------
 *
 * This code is in BETA; some features are incomplete and the code
 * could be written better.
 */

package preparation;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.eesystem.parser4builder.json.structure.BIM;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Калсс загрузки и десериализации ПИМ здания.
 * <p>
 * Created by boris on 13.12.16, modified 22.05.17, 02.04.2018
 */
class BIMLoader {

    BIM getBim(String nFilePath) {
        Logger log = LoggerFactory.getLogger(BIMLoader.class);
        InputStream is = null;
        try {
            is = new FileInputStream(new File(nFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BIM bim = new BIM();
        Class<?> bimClass = bim.getClass();
        String className = bimClass.getSimpleName();
        assert is != null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String s;
            StringBuilder res = new StringBuilder();
            while ((s = br.readLine()) != null) res.append(s);

            if (res.length() == 0) log.error("File *.json is empty", new Error());
            else bim = new Gson().fromJson(res.toString(), (Type) bimClass);

            log.debug("Successful reading json and creating instance for class {} and parse " + "json", className);
        } catch (final IOException e) {
            log.error("Fail: parse json to {} structure. Any problems: ", className, e);
        }

        log.info("BuildingQGis successful loaded from {}", nFilePath);
        return bim;
    }
}
