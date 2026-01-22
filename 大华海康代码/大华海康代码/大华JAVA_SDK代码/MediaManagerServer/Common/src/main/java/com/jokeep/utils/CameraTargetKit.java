package com.jokeep.utils;

public class CameraTargetKit {

    /**
     *
     * @param cameraCoordinate
     * @param cameraHeight
     * @param pitchAngle
     * @param vFov
     * @param turnAngle
     * @param imageSize
     * @param targetPixelCoordinate
     * @return
     */
    public Coordinate getTargetCoordinate(Coordinate cameraCoordinate,double cameraHeight,double pitchAngle,double vFov,double turnAngle,ImageSize imageSize,PixelCoordinate targetPixelCoordinate){
        //斜边长
        double hypotenuse=cameraHeight/Math.cos(pitchAngle);
        //俯仰角对边长度
        double oppositeSide=cameraHeight*Math.tan(pitchAngle);
        //求垂直视场角对边的长度
        double fieldAngleSide=cameraHeight*Math.tan(pitchAngle+vFov)-oppositeSide;
        //目标点距离摄像头水平距离
        double cameraDistance=fieldAngleSide*(imageSize.getHeight()-targetPixelCoordinate.getPixelY())/imageSize.getHeight();

        double arc = 6371.393 * 1000;
        //转为弧度
        turnAngle=Math.toRadians(turnAngle);
        double lon =cameraCoordinate.getLongitude() + cameraDistance * Math.sin(turnAngle) / (arc * Math.cos(cameraCoordinate.getLatitude()) * 2 * Math.PI / 360);
        double lat =cameraCoordinate.getLatitude()+ cameraDistance * Math.cos(turnAngle) / (arc * 2 * Math.PI / 360);

        return new Coordinate(lon, lat);
    }

    public class Coordinate{
        public Coordinate(double longitude,double latitude){
            this.longitude=longitude;
            this.latitude=latitude;
        }

        private double longitude=0;
        private double latitude=0;

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
    }

    public class PixelCoordinate {
        public PixelCoordinate(double pixelX,double pixelY){
            this.pixelX=pixelX;
            this.pixelY=pixelY;
        }
        private double pixelX=0;
        private double pixelY=0;

        public double getPixelX() {
            return pixelX;
        }

        public void setPixelX(double pixelX) {
            this.pixelX = pixelX;
        }

        public double getPixelY() {
            return pixelY;
        }

        public void setPixelY(double pixelY) {
            this.pixelY = pixelY;
        }
    }

    public class ImageSize{
        public ImageSize(double width,double height){
            this.width=width;
            this.height=height;
        }
        private double width=0;
        private double height=0;

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }
}
