package br.gov.mma.facial.util;

import org.opencv.core.Core;

/**
 * Utility class for loading OpenCV libraries.
 * Uses org.openpnp:opencv which bundles native libraries properly
 */
public class OpenCVLoader {

    /**
     * Loads OpenCV using the openpnp library which handles native loading automatically
     * @return true if OpenCV was successfully loaded
     */
    public static boolean loadOpenCV() {
        printEnvironmentInfo();
        
        try {
            System.out.println("🔍 Carregando OpenCV via org.openpnp...");
            // Load the native library - openpnp handles this automatically
            nu.pattern.OpenCV.loadLocally();
            System.out.println("✅ OpenCV carregado com sucesso");
            System.out.println("📊 OpenCV Version: " + Core.VERSION);
            return true;
        } catch (Throwable e) {
            System.err.println("❌ Erro ao carregar OpenCV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Prints detailed environment information for diagnostics
     */
    private static void printEnvironmentInfo() {
        System.out.println("Environment information:");
        System.out.println("  java.library.path = " + System.getProperty("java.library.path"));
        System.out.println("  User directory = " + System.getProperty("user.dir"));
        System.out.println("  Temp directory = " + System.getProperty("java.io.tmpdir"));
    }
}