package domain;

import java.net.Socket;
import java.sql.Time;

public class GpsRecord
{
    /**
     * ID of the device that sent the GPS update.
     */
    private final long deviceId;

    /**
     * IP address that was used to send the server the GPS update.
     */
    private final Socket deviceIp;

    /**
     * time that the latitude and longitude of the device was recorded in Linux
     *   epoch time.
     */
    private final Time samplingTime;

    /**
     * latitude of the device at samplingTime in degrees.
     */
    private final double lat;

    /**
     * longitude of the device at samplingTime in degrees.
     */
    private final double lng;

    /**
     * altitude of the device at samplingTime in meters.
     */
    private final double altitude;

    /**
     * speed of the device at samplingTime in meters per second.
     */
    private final double speed;

    /**
     * instantiates a new GPS record object.
     *
     * @param deviceId ID of the device that sent the GPS update.
     * @param deviceIp IP address that was used to send the server the GPS
     *   update.
     * @param samplingTime time that the latitude and longitude of the device
     *   was recorded.
     * @param lat latitude of the device at samplingTime.
     * @param lng longitude of the device at samplingTime.
     */
    public GpsRecord(long deviceId, Socket deviceIp, Time samplingTime,
            double lat, double lng, double altitude, double speed)
    {
        this.deviceId = deviceId;
        this.deviceIp = deviceIp;
        this.samplingTime = samplingTime;
        this.lat = lat;
        this.lng = lng;
        this.altitude = altitude;
        this.speed = speed;
    }

    public long getDeviceId()
    {
        return deviceId;
    }

    public String getDeviceIp()
    {
        return deviceIp.getInetAddress().getHostAddress()+":"+deviceIp.getPort();
    }

    public long getSamplingTime()
    {
        return samplingTime.getTime()/1000;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLng()
    {
        return lng;
    }

    public double getAltitude()
    {
        return altitude;
    }

    public double getSpeed()
    {
        return speed;
    }
}
