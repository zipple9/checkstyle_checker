package com.allcam.codecheck.checker;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpecifyAnnotationCheck extends AbstractCheck {


    private static Map<String , JSONObject> apiMap = new HashMap<>();

    private Set<String> apiSet = new HashSet<>();
    private String apiDefineUrl;

    private String annotationName;

    public void setAnnotationName(String annotationName) {
        this.annotationName = annotationName;
    }

    public void setApiDefineUrl(String apiDefineUrl) {
        this.apiDefineUrl = apiDefineUrl;
        FileReader fileReader = new FileReader(this.apiDefineUrl);
        String result = fileReader.readString();
        JSONArray apiJsons = JSON.parseArray(result);
        for (int i = 0; i < apiJsons.size(); i++) {
            JSONObject api = apiJsons.getJSONObject(i);
            String servicePkg = api.getString("service");
            String interfaceName = servicePkg.substring(servicePkg.lastIndexOf(".")+1);
            apiSet.add(interfaceName);
            apiMap.put(api.getString("method"),api);
        }
    }


    @Override
    public void visitToken(DetailAST ast) {


        final DetailAST defOrNew = ast.getParent().getParent();
        DetailAST itf = defOrNew.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
        if(itf == null){
            return;
        }

        DetailAST iftName = itf.findFirstToken(TokenTypes.IDENT);
        if(iftName==null){
            return;
        }
        Set<String> interfaceNames = new HashSet<>();
        interfaceNames.add(iftName.getText());
        // 实现多个接口的情况
        DetailAST next = iftName.getNextSibling();
        while(next!=null){
            if(next.getType() == TokenTypes.IMPLEMENTS_CLAUSE){
                interfaceNames.add(next.getText());
            }
            next = next.getNextSibling();
        }
        // 遍历所有接口名
        for (String interfaceName : interfaceNames) {
            // 如果当前检测类的接口在api中
            if(apiSet.contains(interfaceName)){
                // 判断方法是否在apiJson中
                DetailAST mName = ast.findFirstToken(TokenTypes.IDENT);
                if(mName == null){
                    continue;
                }
                JSONObject api = apiMap.get(mName.getText());
                // 方法在apiJson中，且和api的method一致
                if( api !=null && mName.getText().equalsIgnoreCase(api.getString("method"))){
                    if(!AnnotationUtil.containsAnnotation(ast,this.annotationName)){
                        log(ast.getLineNo(), StrUtil.format("{}接口未指定注解{}",mName.getText(),this.annotationName));
                    }

                }
            }
        }


    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]
                {TokenTypes.METHOD_DEF, };
    }

}
