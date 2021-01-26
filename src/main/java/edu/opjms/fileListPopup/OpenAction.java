package edu.opjms.fileListPopup;

import java.io.File;

@FunctionalInterface
interface OpenAction {
    void openFile(File file);
}
