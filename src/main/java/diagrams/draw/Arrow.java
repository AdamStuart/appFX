package diagrams.draw;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import util.LineUtil;

public class Arrow extends Polygon {

    public double rotate;
    public float t;			// 0-1 parameter along the curve
    CubicCurve curve;
    Line line;
    Rotate rz;

    public Arrow( CubicCurve curve, float t) {
        super();
        this.curve = curve;
        this.t = t;
        init();
    }

    public Arrow(Line line, float t) {
        super();
        this.line = line;
        this.t = t;
        init();
    }

    public Arrow(Edge edge, float t) {
        super();
        this.line = line;
        this.t = t;
        init();
    }

    public Arrow( CubicCurve curve, float t, double... arg0) {
        super(arg0);
        this.curve = curve;
        this.t = t;
        init();
    }
    public Arrow(  Line line, float t, double... arg0) {
        super(arg0);
        this.line = line;
        this.t = t;
        init();
    }

//--------------------------------------------------------------------
    private void init() {

        setFill(Color.web("#ff0900"));

        rz = new Rotate();
        rz.setAxis(Rotate.Z_AXIS);
        getTransforms().addAll(rz);

        if (curve == null) updateLine();
        else updateCurve();
    }
 
  //--------------------------------------------------------------------
    public void updateLine() {
    	double ang2 = LineUtil.getEndAngle(line);
    	Point2D pt = LineUtil.midPoint(line, 1.0);
        setTranslateX(pt.getX());
        setTranslateY(pt.getY());

           // arrow origin is top => apply offset
        double offset = -90;
        if( t > 0.5)
            offset = +90;

        rz.setAngle(ang2 + offset);
   }
    
	//--------------------------------------------------------------------
   public void updateCurve() {
        double size = Math.max(curve.getBoundsInLocal().getWidth(), curve.getBoundsInLocal().getHeight());
        double scale = size / 4d;

        Point2D ori = eval(curve, t);
        Point2D tan = evalDt(curve, t).normalize().multiply(scale);

        setTranslateX(ori.getX());
        setTranslateY(ori.getY());

        double angle = Math.atan2( tan.getY(), tan.getX());

        angle = Math.toDegrees(angle);

        // arrow origin is top => apply offset
        double offset = -90;
        if( t > 0.5)
            offset = +90;

        rz.setAngle(angle + offset);

    }

      /**
       * Evaluate the cubic curve at a parameter 0<=t<=1, returns a Point2D
       * @param c the CubicCurve 
       * @param t param between 0 and 1
       * @return a Point2D 
       */
      private Point2D eval(CubicCurve c, float t){
    	  double x = Math.pow(1-t,3)*c.getStartX()+ 3*t*Math.pow(1-t,2)*c.getControlX1()+
                  3*(1-t)*t*t*c.getControlX2()+ Math.pow(t, 3)*c.getEndX();
    	  double y = Math.pow(1-t,3)*c.getStartY()+ 3*t*Math.pow(1-t, 2)*c.getControlY1()+
                  3*(1-t)*t*t*c.getControlY2()+ Math.pow(t, 3)*c.getEndY();
          Point2D p=new Point2D(x,y);
          return p;
      }

      /**
       * Evaluate the tangent of the cubic curve at a parameter 0<=t<=1, returns a Point2D
       * @param c the CubicCurve 
       * @param t param between 0 and 1
       * @return a Point2D 
       */
      private Point2D evalDt(CubicCurve c, float t){
          Point2D p=new Point2D(
        		  -3*Math.pow(1-t,2)*c.getStartX()+3*(Math.pow(1-t, 2)-2*t*(1-t))*c.getControlX1()+
           3*((1-t)*2*t-t*t)*c.getControlX2()+3*Math.pow(t, 2)*c.getEndX(),
                  -3*Math.pow(1-t,2)*c.getStartY()+3*(Math.pow(1-t, 2)-2*t*(1-t))*c.getControlY1()+
            3*((1-t)*2*t-t*t)*c.getControlY2()+3*Math.pow(t, 2)*c.getEndY());
          return p;
      }
}
