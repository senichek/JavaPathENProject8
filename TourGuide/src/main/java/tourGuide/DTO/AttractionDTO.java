package tourGuide.DTO;

import gpsUtil.location.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionDTO {
    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private Double distanceInMiles;
    private int rewardsPoints;
}
