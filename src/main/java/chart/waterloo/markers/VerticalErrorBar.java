/* 
*
 * <http://sigtool.github.io/waterlooFX/>
 *
 * Copyright King's College London  2014. Copyright Malcolm Lidierth 2014-.
 * 
 * @author Malcolm Lidierth <a href="https://github.com/sigtool/waterlooFX/issues"> [Contact]</a>
 * 
 * Project Waterloo is free software:  you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Project Waterloo is distributed in the hope that it will  be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package chart.waterloo.markers;

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * @deprecated
 * @author Malcolm Lidierth
 */
public class VerticalErrorBar extends Path implements CenteredShapeInterface {

    public VerticalErrorBar(double ht, double hb, double w) {   
        getElements().add(new MoveTo(-ht / 2, 0));
        getElements().add(new LineTo(hb / 2, 0));

        getElements().add(new MoveTo(-ht, -w / 2));
        getElements().add(new LineTo(-ht, w / 2));

        getElements().add(new MoveTo(hb, -w / 2));
        getElements().add(new LineTo(hb, w / 2));
    }
}