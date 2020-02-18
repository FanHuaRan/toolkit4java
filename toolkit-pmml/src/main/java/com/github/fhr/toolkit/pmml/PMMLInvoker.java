package com.github.fhr.toolkit.pmml;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.PMMLUtil;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * 参考：https://github.com/jpmml/jpmml-evaluator/blob/master/README.md
 */
public class PMMLInvoker{
    private final ModelEvaluator modelEvaluator;

    private final URL url;

    private PMMLInvoker(URL url){
        this.url = url;
        modelEvaluator = initPMMLModel(url);
    };


    private static   ModelEvaluator initPMMLModel(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }

        try (InputStream inputStream = url.openStream();
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            PMML pmml = PMMLUtil.unmarshal(bufferedInputStream);
            ModelEvaluator initModelEvaluator = ModelEvaluatorFactory
                    .newInstance()
                    .newModelEvaluator(pmml);
            initModelEvaluator.verify();
            return initModelEvaluator;
        } catch (Exception e) {
            throw new RuntimeException("init PMML model fail", e);
        }

    }


    public Map<FieldName, Object> invoke(Map<FieldName, Object> paramsMap) {
        if (this.modelEvaluator == null){
            throw new RuntimeException("PMML model is not success init");
        }
        return this.modelEvaluator.evaluate(paramsMap);
    }

}
