/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat.features.steps;

import java.util.Map;

/**
 *
 * @author pwood
 */
public interface RelativePositionChecker {

    void check(Map position, double latitude, double longitude);
    
}
