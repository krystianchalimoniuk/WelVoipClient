package welvoipclient.com.welvoipclient;


import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Krystiano on 2016-11-28.
 */

public class Contacts extends SugarRecord {
    @Unique
     String first_name;
     String last_name;
     String ip_address;

    public Contacts(){
    }

    public Contacts (String first_name,String last_name,String ip_address){
        this.first_name=first_name;
        this.last_name=last_name;
        this.ip_address=ip_address;

    }

}
