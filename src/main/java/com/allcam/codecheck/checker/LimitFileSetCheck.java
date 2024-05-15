package com.allcam.codecheck.checker;

import java.io.File;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.*;

public class LimitFileSetCheck extends AbstractFileSetCheck {
    private static final int DEFAULT_MAX = 1000;
    private int fileCount;
    private int max = DEFAULT_MAX;

    public void setMax(int aMax) {
        this.max = aMax;
    }

    @Override
    public void beginProcessing(String aCharset) {
        super.beginProcessing(aCharset);

        //reset the file count
        this.fileCount = 0;
    }

    @Override
    protected void processFiltered(File file, FileText fileText) throws CheckstyleException {

    }


}
