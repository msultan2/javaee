/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.repositories;

import com.ssl.rmas.entities.Device;
import com.ssl.rmas.entities.DeviceFilter;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.security.RMASUserDetails;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@PropertySource("classpath:config/application.properties")
public class DeviceRepositoryImpl implements DeviceRepositoryCustom {

    private final Logger logger = LoggerFactory.getLogger(DeviceRepositoryImpl.class);
    private MongoTemplate mongoTemplate;

    @Value("${rmas.device.filter.maxResults}")
    private Integer maxNoDevices;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Device> findDevicesForCurrentUser(Optional<List<DeviceFilter>> filters) throws IllegalAccessException {
        Criteria orCriteria = new Criteria();
        if (filters.isPresent()) {
            orCriteria.orOperator(getAndCriterias(filters.get()));
        }
        Criteria finalCriteria = new Criteria();
        finalCriteria.andOperator(orCriteria, getUserGroupFilterCriteria());
        Query query = new Query(finalCriteria);
        query.limit(maxNoDevices + 1);
        return mongoTemplate.find(query, Device.class);
    }

    private Criteria[] getAndCriterias(List<DeviceFilter> filters) throws IllegalAccessException {
        List<Criteria> andCriterias = new ArrayList<>();
        for (DeviceFilter filter : filters) {
            andCriterias.add(getAndCriteria(filter));
        }
        return andCriterias.toArray(new Criteria[andCriterias.size()]);
    }

    private Criteria getAndCriteria(DeviceFilter filter) throws IllegalAccessException {
        Field[] fields = DeviceFilter.class.getDeclaredFields();
        Criteria andCriteria = new Criteria();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.get(filter) != null) {
                logger.debug("Fields added to Criteria are {} : {}", field.getName(), field.get(filter));
                andCriteria.and(field.getName()).is(field.get(filter));
            }
        }
        return andCriteria;
    }

    private Criteria getUserGroupFilterCriteria() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        RMASUserDetails userDetails = (RMASUserDetails) authentication.getPrincipal();
        User user = userDetails.getBaseUser();
        List<String> deviceFilterConditions = user.getUserGroup().getDeviceFilter().conditions();
        return createCriteria(deviceFilterConditions);
    }

    private Criteria createCriteria(List<String> deviceFilterConditions) {
        Criteria criteria = new Criteria();
        deviceFilterConditions.stream()
            .map(filterCondition -> filterCondition.substring("DEVICE_FILTER_CONDITION_".length()))
            .map(filterConditionKeyAndValue -> filterConditionKeyAndValue.split("="))
            .forEach(filterConditionKeyAndValueParts -> criteria.and(filterConditionKeyAndValueParts[0]).is(filterConditionKeyAndValueParts[1]));
        return criteria;
    }

    @Override
    public Device findOne(final String ip) {
        List<Device> findDevicesForCurrentUser = findDevicesForCurrentUser(ip);
        int nOfDevices = findDevicesForCurrentUser.size();
        return nOfDevices == 1 ? findDevicesForCurrentUser.get(0) : null;
    }

    private List<Device> findDevicesForCurrentUser(final String ip) {
        Optional<List<DeviceFilter>> filterForIp = createFilterForIp(ip);
        try {
            return findDevicesForCurrentUser(filterForIp);
        } catch (IllegalAccessException ex) {
            return Collections.emptyList();
        }
    }

    private Optional<List<DeviceFilter>> createFilterForIp(final String id) {
        DeviceFilter deviceFilter = new DeviceFilter();
        deviceFilter.setIpAddress(id);
        List<DeviceFilter> filterList = Collections.singletonList(deviceFilter);
        return Optional.of(filterList);
    }

}
