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
package chart.waterloo.plot;

import java.util.ArrayList;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 *
 * @author Malcolm Lidierth
 * @param <T>
 */
abstract public class AbstractBox<T extends Shape> extends AbstractPlot<ArrayList<Shape>> implements MarkerInterface {

    /**
     * Default constructor.
     *
     */
    public AbstractBox() {
        visualElement = new ArrayList<>();
        visualModel.setMarkerTemplate(new Rectangle());
    }

    /**
     * Constructs an instance parenting another plot.
     *
     * <em>The data model of the child will be copied by reference to the new
     * parent instance.</em>
     *
     * Compound plots that share a data model may therefore be constructed by
     * chaining constructor calls, e.g.:
     * <p>
     * {@code GJScatter = new GJScatter(new GJLine(new GJErrorBar));}
     * </p>
     *
     * Further plots may be added by calling the {@code add(AbstractPlot p)}
     * method and will also share the data model. Note that data model are
     * <strong>not</strong> shared when using the standard
     * <p>
     * {@code getChildren().add(...)}
     * </p>
     * method.
     *
     * @param p1 the child plot to add to this instance.
     */
    public AbstractBox(AbstractPlot p1) {
        this();
        super.add(p1);
    }

    @Override
    public void arrangePlot(Chart chart) {
        super.arrangePlot(chart);
    }

    @Override
    public boolean isValid() {
        return dataModel.getYData().size() > 0
                && dataModel.getXData().size() == dataModel.getYData().size()
                && dataModel.getXData().size() == dataModel.getExtraData0().size() || dataModel.getExtraData0().isEmpty()
                && dataModel.getXData().size() == dataModel.getExtraData1().size() || dataModel.getExtraData1().isEmpty()
                && dataModel.getXData().size() == dataModel.getExtraData2().size() || dataModel.getExtraData2().isEmpty()
                && dataModel.getXData().size() == dataModel.getExtraData3().size() || dataModel.getExtraData3().isEmpty();
    }
}
