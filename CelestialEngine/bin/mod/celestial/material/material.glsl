
#proceduralBSDF_V

vec4 worldPosition4f = WORLD_POSITION;
vec3 worldPosition = worldPosition4f.xyz;
if(uni_materialRestrictTangents > 0.5) {
	att_materialNorm = (uni_transform * vec4(in_normal, 0.0)).xyz;
	att_materialView = (inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition;
	for(int i = 0 ; i < uni_materialLightMax ; ++i) {
		att_materialSAng[i] = uni_materialPos[i].xyz - worldPosition;
		att_materialVDir[i] = uni_materialDir[i];
	}
}
else {
	vec3 surfaceNormal = (uni_transform * vec4(in_normal, 0)).xyz;
	vec3 norm = normalize(surfaceNormal), tang = normalize((uni_transform * vec4(in_tangent, 0)).xyz), bitang = normalize(cross(norm, tang));
	
	mat3 tangSpace = mat3(
		tang.x, bitang.x, norm.x,
		tang.y, bitang.y, norm.y,
		tang.z, bitang.z, norm.z
	);
	
	att_materialView = tangSpace * ((inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition);
	for(int i = 0 ; i < uni_materialLightMax ; ++i) {
		att_materialSAng[i] = tangSpace * (uni_materialPos[i].xyz - worldPosition);
		att_materialVDir[i] = tangSpace * uni_materialDir[i];
	}
}

const float transition = 0.92;
float distance = length((uni_viewMatrix * worldPosition4f).xyz);

for(int i = 0 ; i < uni_materialShadowCount ; ++i) {
	att_materialShadowCoords[i] = uni_materialShadowMatrix[i] * worldPosition4f;
	if(uni_materialShadowMaxDistance[i] > 0)
		att_materialShadowTransition[i] = clamp(1.0 - ((distance - uni_materialShadowMaxDistance[i] * transition) / ((1.0 - transition) * uni_materialShadowMaxDistance[i])), 0.0, 1.0);
	else
		att_materialShadowTransition[i] = 1.0;
}
if(uni_materialHasPointShadow > 0.5) {
	att_materialPointDistance = length(worldPosition - uni_materialPos[int(uni_materialPointShadowIndex)]) / uni_materialFarPlane;
	att_materialPointDirection = -(uni_materialPos[int(uni_materialPointShadowIndex)].xyz - worldPosition);
}

#proceduralBSDF_F

\#define GEOMETRY_GGX(_ggxID, NdotV, roughness)		\
{													\
	float r = roughness + 1.0;						\
	float k = (r * r) / 8.0;						\
													\
	float num = NdotV;								\
	float denom = NdotV * (1.0 - k) + k;			\
													\
	_ggxID = num / denom;							\
}

const float PI = 3.14159265359;

float metallic = uni_materialMetallic * glb_materialMetallic;
float roughness = uni_materialRoughness * glb_materialRoughness;
float subsurfaceVal = uni_materialSubsurface * glb_materialSubsurface;
float ao = uni_materialAO * glb_materialAO;

vec3 unorm = normalize(uni_materialRestrictTangents > 0.5 ? att_materialNorm : glb_surfaceNorm);
vec3 uview = normalize(att_materialView);

int shadowIndex = 0;
vec3 influence = vec3(0.0);
vec3 subsurface = vec3(0.0);
for(int i = 0 ; i < uni_materialLightMax ; ++i) {
	bool sunlight = (int(uni_materialArgs[i].x) & 16) > 0;
	
	float shadowFactor = 1.0;
	if(uni_materialShadowIndices[shadowIndex] == i && shadowIndex < uni_materialShadowCount) {
		vec3 shadowCoords = (att_materialShadowCoords[shadowIndex].xyz / att_materialShadowCoords[shadowIndex].w) * 0.5 + 0.5;
		
		if(sunlight) {
			int pcfCount = sunlight ? 1 : 0;
			float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
			float mapSize = uni_materialMapSize[i], texelSize = 1.0 / mapSize, total = 0.0;
			
			for(int x = -pcfCount ; x <= pcfCount ; ++x) {
				for(int y = -pcfCount ; y <= pcfCount ; ++y) {
					float objectNearestLight = texture(uni_materialShadowMap[shadowIndex], shadowCoords.xy + vec2(x, y) * texelSize).r;
					if(shadowCoords.z > objectNearestLight + 0.001)
						total++;
				}
			}
			shadowFactor = 1.0 - (att_materialShadowTransition[shadowIndex] * (total / totalTexels));
		}
		else {
			float objectNearestLight = texture(uni_materialShadowMap[shadowIndex], shadowCoords.xy).r;
			if(shadowCoords.z > objectNearestLight + 0.001)
				shadowFactor = 0.0;
		}
		
		shadowIndex++;
	}
	else if(uni_materialHasPointShadow > 0.5 && uni_materialPointShadowIndex == i && att_materialPointDistance > texture(uni_materialPointShadowMap, normalize(att_materialPointDirection)).r + 0.0025)
		shadowFactor = 0.0;
	if(shadowFactor == 0.0)
		continue;
	
	bool noAtt = (int(uni_materialArgs[i].x) & 1) > 0, noCosTheta = (int(uni_materialArgs[i].x) & 2) > 0;
	bool spotlight = (int(uni_materialArgs[i].x) & 4) > 0, arealight = (int(uni_materialArgs[i].x) & 8) > 0;
	
	vec3 toLight = att_materialSAng[i];
	if(arealight && !spotlight) {
		vec3 planeForward = att_materialVDir[i];
		vec3 planeUp = vec3(planeForward.x, -planeForward.z, planeForward.y);
		vec3 planeRight = cross(planeForward, planeUp);
		
		vec3 closestX = dot(-att_materialSAng[i], planeRight) * planeRight;
		float boundX = length(planeRight * uni_materialArgs[i].y);
		if(length(closestX) > boundX) closestX = normalize(closestX) * boundX;
		vec3 closestY = dot(-att_materialSAng[i], planeUp) * planeUp;
		float boundY = length(planeUp * uni_materialArgs[i].z);
		if(length(closestY) > boundY) closestY = normalize(closestY) * boundY;
		toLight = closestX + closestY + att_materialSAng[i];
	}
	else if(sunlight)
		toLight = -att_materialVDir[i];
	
	vec3 uAng = normalize(toLight);
	vec3 halfway = normalize(uview + uAng);
	
	float distance = length(toLight);
	float att = noAtt ? 1.0 : max(uni_materialAtt[i] / (distance * distance), 0.0);
	vec3 radiance = uni_materialCol[i] * att * shadowFactor;
	
	vec3 f0 = vec3(uni_materialF0);
	f0 = mix(f0, out_Color.rgb, clamp(metallic + uni_materialSpecularTint, 0.0, 1.0));
	vec3 ratio = f0 + (1 - f0) * pow(max(1.0 - dot(halfway, uview), 0.0), 5.0);
	
	float distribution;
	float geometry;
	
	/* Distribution GGX --- Microfacet distruction function */
	{
		float a = roughness * roughness;
		float a_2 = a * a;
		float NdotH = max(dot(unorm, halfway), 0.0);
		float NdotH_2 = NdotH * NdotH;
		
		float num = a_2;
		float denom = (NdotH_2 * (a_2 - 1.0) + 1.0);
		denom = PI * denom * denom;
		
		distribution = num / denom;
	}
	
	/* Geometry Smith --- Geometry shadowing function */
	{
		float NdotV = max(dot(unorm, uview), 0.0);
		float NdotL = max(dot(unorm, uAng), 0.0);
		float ggx2, ggx1;
		GEOMETRY_GGX(ggx2, NdotV, roughness);
		GEOMETRY_GGX(ggx1, NdotL, roughness);
		
		geometry = ggx1 * ggx2;
	}
	
	vec3 num = distribution * geometry * ratio;
	float denom = 4.0 * max(dot(unorm, uview), 0.0) * max(dot(unorm, uAng), 0.0);
	vec3 specular = num / max(denom, 0.001) * uni_materialSpecular;
	
	vec3 kSpecular = ratio;
	vec3 kDiffuse = (vec3(1.0) - kSpecular) * (1.0 - metallic);
	
	float NdotL = (noCosTheta || uni_materialNoCosTheta > 0.5) ? 1.0 : max(dot(unorm, uAng), 0.0);
	if(length(att_materialVDir[i]) > 0.0 && !sunlight) {
		if(spotlight) {
			float outerCutoff = cos(radians(abs(uni_materialArgs[i].y)));
			float innerCutoff = cos(radians(abs(uni_materialArgs[i].y)) * (1 - clamp(uni_materialArgs[i].z, 0.0, 1.0)));
			float theta = dot(uAng, normalize(-att_materialVDir[i]));
			float epsilon = innerCutoff - outerCutoff;
			float spotIntensity = clamp((theta - outerCutoff) / epsilon, 0.0, 1.0);
			
			NdotL *= spotIntensity;
		}
		else NdotL *= pow(max(dot(-uAng, normalize(att_materialVDir[i])), 0.0), !arealight ? uni_materialArgs[i].y : 1.0);
	}
	
	influence += (kDiffuse * out_Color.rgb / PI + specular) * radiance * NdotL;
	
	if(subsurfaceVal > 0.0) {
		vec3 light = normalize(uAng + (unorm * uni_materialSubsurfaceDistortion));
		float dot = pow(clamp(dot(uview, -light), 0.0, 1.0), 4.0) * uni_materialSubsurfaceScale;
		float lt = att * (dot + uni_materialSubsurfaceAmbient) * uni_materialSubsurfaceThickness;
		subsurface += uni_materialCol[i] * uni_materialSubsurfaceRadius * lt * subsurfaceVal;
	}
}

vec3 ambient = vec3(uni_materialAmbient) * out_Color.rgb * ao;
vec3 color = ambient + influence + subsurface;
color = color / (color + vec3(1.0));
color = pow(color, vec3(1.0 / uni_materialGamma));

out_Color = vec4(color, out_Color.a);

#interpolatedBSDF_V

\#define GEOMETRY_GGX(_ggxID, NdotV, roughness)		\
{													\
	float r = roughness + 1.0;						\
	float k = (r * r) / 8.0;						\
													\
	float num = NdotV;								\
	float denom = NdotV * (1.0 - k) + k;			\
													\
	_ggxID = num / denom;							\
}

const float PI = 3.14159265359;

const float transition = 0.92;
vec4 worldPosition4f = WORLD_POSITION;
vec3 worldPosition = worldPosition4f.xyz;
float camDistance = length((uni_viewMatrix * worldPosition4f).xyz);

vec3 unorm = normalize((uni_transform * vec4(in_normal, 0.0)).xyz);
vec3 uview = normalize((inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition);

int shadowIndex = 0;
vec3 subsurface = vec3(0.0);
for(int i = 0 ; i < uni_materialLightMax ; ++i) {
	bool sunlight = (int(uni_materialArgs[i].x) & 16) > 0;
	
	float shadowFactor = 1.0;
	if(uni_materialShadowIndices[shadowIndex] == i && shadowIndex < uni_materialShadowCount) {
		vec4 shadowCoords4f = uni_materialShadowMatrix[i] * worldPosition4f;
		float shadowTransition = 1.0;
		if(uni_materialShadowMaxDistance[i] > 0)
			shadowTransition = clamp(1.0 - ((camDistance - uni_materialShadowMaxDistance[i] * transition) / ((1.0 - transition) * uni_materialShadowMaxDistance[i])), 0.0, 1.0);
		vec3 shadowCoords = (shadowCoords4f.xyz / shadowCoords4f.w) * 0.5 + 0.5;
		
		if(sunlight) {
			int pcfCount = sunlight ? 1 : 0;
			float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
			float mapSize = uni_materialMapSize[i], texelSize = 1.0 / mapSize, total = 0.0;
			
			for(int x = -pcfCount ; x <= pcfCount ; ++x) {
				for(int y = -pcfCount ; y <= pcfCount ; ++y) {
					float objectNearestLight = texture(uni_materialShadowMap[shadowIndex], shadowCoords.xy + vec2(x, y) * texelSize).r;
					if(shadowCoords.z > objectNearestLight + 0.001)
						total++;
				}
			}
			shadowFactor = 1.0 - (shadowTransition * (total / totalTexels));
		}
		else {
			float objectNearestLight = texture(uni_materialShadowMap[shadowIndex], shadowCoords.xy).r;
			if(shadowCoords.z > objectNearestLight + 0.001)
				shadowFactor = 0.0;
		}
		
		shadowIndex++;
	}
	else if(uni_materialHasPointShadow > 0.5 && uni_materialPointShadowIndex == i) {
		float distance = length(worldPosition - uni_materialPos[int(uni_materialPointShadowIndex)]) / uni_materialFarPlane;
		vec3 direction = -(uni_materialPos[int(uni_materialPointShadowIndex)].xyz - worldPosition);
		if(distance > texture(uni_materialPointShadowMap, normalize(direction)).r + 0.0025)
			shadowFactor = 0.0;
	}
	if(shadowFactor == 0.0) {
		att_materialRatioMul[i] = 0.0;
		att_materialInfluenceMul[i] = vec3(0.0);
		att_materialSpecularMul[i] = vec3(0.0);
		continue;
	}
	
	vec3 sAng = uni_materialPos[i].xyz - worldPosition;
	bool noAtt = (int(uni_materialArgs[i].x) & 1) > 0, noCosTheta = (int(uni_materialArgs[i].x) & 2) > 0;
	bool spotlight = (int(uni_materialArgs[i].x) & 4) > 0, arealight = (int(uni_materialArgs[i].x) & 8) > 0;
	
	vec3 toLight = sAng;
	if(arealight && !spotlight) {
		vec3 planeForward = uni_materialDir[i];
		vec3 planeUp = vec3(planeForward.x, -planeForward.z, planeForward.y);
		vec3 planeRight = cross(planeForward, planeUp);
		
		vec3 closestX = dot(-sAng, planeRight) * planeRight;
		float boundX = length(planeRight * uni_materialArgs[i].y);
		if(length(closestX) > boundX) closestX = normalize(closestX) * boundX;
		vec3 closestY = dot(-sAng, planeUp) * planeUp;
		float boundY = length(planeUp * uni_materialArgs[i].z);
		if(length(closestY) > boundY) closestY = normalize(closestY) * boundY;
		toLight = closestX + closestY + sAng;
	}
	else if(sunlight)
		toLight = -uni_materialDir[i];
	
	vec3 uAng = normalize(toLight);
	vec3 halfway = normalize(uview + uAng);
	
	float distance = length(toLight);
	float att = noAtt ? 1.0 : max(uni_materialAtt[i] / (distance * distance), 0.0);
	vec3 radiance = uni_materialCol[i] * att * shadowFactor;
	
	att_materialRatioMul[i] = pow(max(1.0 - dot(halfway, uview), 0.0), 5.0);
	
	float distribution;
	float geometry;
	
	/* Distribution GGX --- Microfacet distruction function */
	{
		float a = uni_materialRoughness * uni_materialRoughness;
		float a_2 = a * a;
		float NdotH = max(dot(unorm, halfway), 0.0);
		float NdotH_2 = NdotH * NdotH;
		
		float num = a_2;
		float denom = (NdotH_2 * (a_2 - 1.0) + 1.0);
		denom = PI * denom * denom;
		
		distribution = num / denom;
	}
	
	/* Geometry Smith --- Geometry shadowing function */
	{
		float NdotV = max(dot(unorm, uview), 0.0);
		float NdotL = max(dot(unorm, uAng), 0.0);
		float ggx2, ggx1;
		GEOMETRY_GGX(ggx2, NdotV, uni_materialRoughness);
		GEOMETRY_GGX(ggx1, NdotL, uni_materialRoughness);
		
		geometry = ggx1 * ggx2;
	}
	
	vec3 num = distribution * geometry * vec3(1);
	float denom = 4.0 * max(dot(unorm, uview), 0.0) * max(dot(unorm, uAng), 0.0);
	att_materialSpecularMul[i] = num / max(denom, 0.001) * uni_materialSpecular;
	
	float NdotL = (noCosTheta || uni_materialNoCosTheta > 0.5) ? 1.0 : max(dot(unorm, uAng), 0.0);
	if(length(uni_materialDir[i]) > 0.0 && !sunlight) {
		if(spotlight) {
			float outerCutoff = cos(radians(abs(uni_materialArgs[i].y)));
			float innerCutoff = cos(radians(abs(uni_materialArgs[i].y)) * (1 - clamp(uni_materialArgs[i].z, 0.0, 1.0)));
			float theta = dot(uAng, normalize(-uni_materialDir[i]));
			float epsilon = innerCutoff - outerCutoff;
			float spotIntensity = clamp((theta - outerCutoff) / epsilon, 0.0, 1.0);
			
			NdotL *= spotIntensity;
		}
		else NdotL *= pow(max(dot(-uAng, normalize(uni_materialDir[i])), 0.0), !arealight ? uni_materialArgs[i].y : 1.0);
	}
	att_materialInfluenceMul[i] = radiance * NdotL;
	
	if(uni_materialSubsurface > 0.0) {
		vec3 light = normalize(uAng + (unorm * uni_materialSubsurfaceDistortion));
		float dot = pow(clamp(dot(uview, -light), 0.0, 1.0), 4.0) * uni_materialSubsurfaceScale;
		float lt = att * (dot + uni_materialSubsurfaceAmbient) * uni_materialSubsurfaceThickness;
		subsurface += uni_materialCol[i] * uni_materialSubsurfaceRadius * lt * uni_materialSubsurface;
	}
}

att_materialSubsurface = subsurface;

#interpolatedBSDF_F

const float PI = 3.14159265359;

float metallic = uni_materialMetallic * glb_materialMetallic;
float ao = uni_materialAO * glb_materialAO;

vec3 influence = vec3(0.0);
for(int i = 0 ; i < uni_materialLightMax ; ++i) {
	vec3 f0 = vec3(uni_materialF0);
	f0 = mix(f0, out_Color.rgb, clamp(uni_materialMetallic + uni_materialSpecularTint, 0.0, 1.0));
	vec3 ratio = f0 + (1 - f0) * att_materialRatioMul[i];
	
	vec3 specular = att_materialSpecularMul[i] * ratio;
	vec3 kSpecular = ratio;
	vec3 kDiffuse = (vec3(1.0) - kSpecular) * (1.0 - uni_materialMetallic);
	
	influence += (kDiffuse * out_Color.rgb / PI + specular) * att_materialInfluenceMul[i];
}

vec3 ambient = vec3(uni_materialAmbient) * out_Color.rgb * ao;
vec3 color = ambient + influence + att_materialSubsurface;
color = color / (color + vec3(1.0));
color = pow(color, vec3(1.0 / uni_materialGamma));

out_Color = vec4(color, out_Color.a);

// RDF Materials --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

#proceduralRDF_V

vec4 worldPosition4f = WORLD_POSITION;
vec3 worldPosition = worldPosition4f.xyz;
if(uni_materialRestrictTangents > 0.5) {
	att_materialNorm = (uni_transform * vec4(in_normal, 0.0)).xyz;
	att_materialView = (inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition;
	for(int i = 0 ; i < uni_materialLightMax ; ++i) {
		att_materialSAng[i] = uni_materialPos[i].xyz - worldPosition;
		att_materialVDir[i] = uni_materialDir[i];
	}
}
else {
	vec3 surfaceNormal = (uni_transform * vec4(in_normal, 0)).xyz;
	vec3 norm = normalize(surfaceNormal), tang = normalize((uni_transform * vec4(in_tangent, 0)).xyz), bitang = normalize(cross(norm, tang));
	
	mat3 tangSpace = mat3(
		tang.x, bitang.x, norm.x,
		tang.y, bitang.y, norm.y,
		tang.z, bitang.z, norm.z
	);
	
	att_materialView = tangSpace * ((inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition);
	for(int i = 0 ; i < uni_materialLightMax ; ++i) {
		att_materialSAng[i] = tangSpace * (uni_materialPos[i].xyz - worldPosition);
		att_materialVDir[i] = tangSpace * uni_materialDir[i];
	}
}

const float transition = 0.92;
float distance = length((uni_viewMatrix * worldPosition4f).xyz);

for(int i = 0 ; i < uni_materialShadowCount ; ++i) {
	att_materialShadowCoords[i] = uni_materialShadowMatrix[i] * worldPosition4f;
	if(uni_materialShadowMaxDistance[i] > 0)
		att_materialShadowTransition[i] = clamp(1.0 - ((distance - uni_materialShadowMaxDistance[i] * transition) / ((1.0 - transition) * uni_materialShadowMaxDistance[i])), 0.0, 1.0);
	else
		att_materialShadowTransition[i] = 1.0;
}
if(uni_materialHasPointShadow > 0.5) {
	att_materialPointDistance = length(worldPosition - uni_materialPos[int(uni_materialPointShadowIndex)]) / uni_materialFarPlane;
	att_materialPointDirection = -(uni_materialPos[int(uni_materialPointShadowIndex)].xyz - worldPosition);
}

#proceduralRDF_F

const float PI = 3.14159265359;

float ao = uni_materialAO * glb_materialAO;

vec3 unorm = normalize(uni_materialRestrictTangents > 0.5 ? att_materialNorm : glb_surfaceNorm);
vec3 uview = normalize(att_materialView);

int shadowIndex = 0;
vec3 influence = vec3(0.0);
for(int i = 0 ; i < uni_materialLightMax ; ++i) {
	bool sunlight = (int(uni_materialArgs[i].x) & 16) > 0;
	
	float shadowFactor = 1.0;
	if(uni_materialShadowIndices[shadowIndex] == i && shadowIndex < uni_materialShadowCount) {
		vec3 shadowCoords = (att_materialShadowCoords[shadowIndex].xyz / att_materialShadowCoords[shadowIndex].w) * 0.5 + 0.5;
		
		if(sunlight) {
			int pcfCount = sunlight ? 1 : 0;
			float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
			float mapSize = uni_materialMapSize[i], texelSize = 1.0 / mapSize, total = 0.0;
			
			for(int x = -pcfCount ; x <= pcfCount ; ++x) {
				for(int y = -pcfCount ; y <= pcfCount ; ++y) {
					float objectNearestLight = texture(uni_materialShadowMap[shadowIndex], shadowCoords.xy + vec2(x, y) * texelSize).r;
					if(shadowCoords.z > objectNearestLight + 0.001)
						total++;
				}
			}
			shadowFactor = 1.0 - (att_materialShadowTransition[shadowIndex] * (total / totalTexels));
		}
		else {
			float objectNearestLight = texture(uni_materialShadowMap[shadowIndex], shadowCoords.xy).r;
			if(shadowCoords.z > objectNearestLight + 0.001)
				shadowFactor = 0.0;
		}
		
		shadowIndex++;
	}
	else if(uni_materialHasPointShadow > 0.5 && uni_materialPointShadowIndex == i && att_materialPointDistance > texture(uni_materialPointShadowMap, normalize(att_materialPointDirection)).r + 0.0025)
		shadowFactor = 0.0;
	if(shadowFactor == 0.0)
		continue;
	
	bool noAtt = (int(uni_materialArgs[i].x) & 1) > 0, noCosTheta = (int(uni_materialArgs[i].x) & 2) > 0;
	bool spotlight = (int(uni_materialArgs[i].x) & 4) > 0, arealight = (int(uni_materialArgs[i].x) & 8) > 0;
	
	vec3 toLight = att_materialSAng[i];
	if(arealight && !spotlight) {
		vec3 planeForward = att_materialVDir[i];
		vec3 planeUp = vec3(planeForward.x, -planeForward.z, planeForward.y);
		vec3 planeRight = cross(planeForward, planeUp);
		
		vec3 closestX = dot(-att_materialSAng[i], planeRight) * planeRight;
		float boundX = length(planeRight * uni_materialArgs[i].y);
		if(length(closestX) > boundX) closestX = normalize(closestX) * boundX;
		vec3 closestY = dot(-att_materialSAng[i], planeUp) * planeUp;
		float boundY = length(planeUp * uni_materialArgs[i].z);
		if(length(closestY) > boundY) closestY = normalize(closestY) * boundY;
		toLight = closestX + closestY + att_materialSAng[i];
	}
	else if(sunlight)
		toLight = -att_materialVDir[i];
	
	vec3 uAng = normalize(toLight);
	vec3 halfway = normalize(uview + uAng);
	
	float distance = length(toLight);
	float att = noAtt ? 1.0 : max(uni_materialAtt[i] / (distance * distance), 0.0);
	vec3 radiance = uni_materialCol[i] * att * shadowFactor;
	
	vec3 kDiffuse = vec3(1.0);
	
	float NdotL = (noCosTheta || uni_materialNoCosTheta > 0.5) ? 1.0 : max(dot(unorm, uAng), 0.0);
	if(length(att_materialVDir[i]) > 0.0 && !sunlight) {
		if(spotlight) {
			float outerCutoff = cos(radians(abs(uni_materialArgs[i].y)));
			float innerCutoff = cos(radians(abs(uni_materialArgs[i].y)) * (1 - clamp(uni_materialArgs[i].z, 0.0, 1.0)));
			float theta = dot(uAng, normalize(-att_materialVDir[i]));
			float epsilon = innerCutoff - outerCutoff;
			float spotIntensity = clamp((theta - outerCutoff) / epsilon, 0.0, 1.0);
			
			NdotL *= spotIntensity;
		}
		else NdotL *= pow(max(dot(-uAng, normalize(att_materialVDir[i])), 0.0), !arealight ? uni_materialArgs[i].y : 1.0);
	}
	influence += (kDiffuse * out_Color.rgb / PI) * radiance * NdotL;
}

vec3 ambient = vec3(uni_materialAmbient) * out_Color.rgb * ao;
vec3 color = ambient + influence;
color = color / (color + vec3(1.0));
color = pow(color, vec3(1.0 / uni_materialGamma));

out_Color = vec4(color, out_Color.a);

#interpolatedRDF_V

const float transition = 0.92;
vec4 worldPosition4f = WORLD_POSITION;
vec3 worldPosition = worldPosition4f.xyz;
float camDistance = length((uni_viewMatrix * worldPosition4f).xyz);

vec3 unorm = normalize((uni_transform * vec4(in_normal, 0.0)).xyz);
vec3 uview = normalize((inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition);

int shadowIndex = 0;
for(int i = 0 ; i < uni_materialLightMax ; ++i) {
	bool sunlight = (int(uni_materialArgs[i].x) & 16) > 0;
	
	float shadowFactor = 1.0;
	if(uni_materialShadowIndices[shadowIndex] == i && shadowIndex < uni_materialShadowCount) {
		vec4 shadowCoords4f = uni_materialShadowMatrix[i] * worldPosition4f;
		float shadowTransition = 1.0;
		if(uni_materialShadowMaxDistance[i] > 0)
			shadowTransition = clamp(1.0 - ((camDistance - uni_materialShadowMaxDistance[i] * transition) / ((1.0 - transition) * uni_materialShadowMaxDistance[i])), 0.0, 1.0);
		vec3 shadowCoords = (shadowCoords4f.xyz / shadowCoords4f.w) * 0.5 + 0.5;
		
		if(sunlight) {
			int pcfCount = sunlight ? 1 : 0;
			float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
			float mapSize = uni_materialMapSize[i], texelSize = 1.0 / mapSize, total = 0.0;
			
			for(int x = -pcfCount ; x <= pcfCount ; ++x) {
				for(int y = -pcfCount ; y <= pcfCount ; ++y) {
					float objectNearestLight = texture(uni_materialShadowMap[shadowIndex], shadowCoords.xy + vec2(x, y) * texelSize).r;
					if(shadowCoords.z > objectNearestLight + 0.001)
						total++;
				}
			}
			shadowFactor = 1.0 - (shadowTransition * (total / totalTexels));
		}
		else {
			float objectNearestLight = texture(uni_materialShadowMap[shadowIndex], shadowCoords.xy).r;
			if(shadowCoords.z > objectNearestLight + 0.001)
				shadowFactor = 0.0;
		}
		
		shadowIndex++;
	}
	else if(uni_materialHasPointShadow > 0.5 && uni_materialPointShadowIndex == i) {
		float distance = length(worldPosition - uni_materialPos[int(uni_materialPointShadowIndex)]) / uni_materialFarPlane;
		vec3 direction = -(uni_materialPos[int(uni_materialPointShadowIndex)].xyz - worldPosition);
		if(distance > texture(uni_materialPointShadowMap, normalize(direction)).r + 0.0025)
			shadowFactor = 0.0;
	}
	if(shadowFactor == 0.0) {
		att_materialInfluenceMul[i] = vec3(0.0);
		continue;
	}
	
	vec3 sAng = uni_materialPos[i].xyz - worldPosition;
	bool noAtt = (int(uni_materialArgs[i].x) & 1) > 0, noCosTheta = (int(uni_materialArgs[i].x) & 2) > 0;
	bool spotlight = (int(uni_materialArgs[i].x) & 4) > 0, arealight = (int(uni_materialArgs[i].x) & 8) > 0;
	
	vec3 toLight = sAng;
	if(arealight && !spotlight) {
		vec3 planeForward = uni_materialDir[i];
		vec3 planeUp = vec3(planeForward.x, -planeForward.z, planeForward.y);
		vec3 planeRight = cross(planeForward, planeUp);
		
		vec3 closestX = dot(-sAng, planeRight) * planeRight;
		float boundX = length(planeRight * uni_materialArgs[i].y);
		if(length(closestX) > boundX) closestX = normalize(closestX) * boundX;
		vec3 closestY = dot(-sAng, planeUp) * planeUp;
		float boundY = length(planeUp * uni_materialArgs[i].z);
		if(length(closestY) > boundY) closestY = normalize(closestY) * boundY;
		toLight = closestX + closestY + sAng;
	}
	else if(sunlight)
		toLight = -uni_materialDir[i];
	
	vec3 uAng = normalize(toLight);
	vec3 halfway = normalize(uview + uAng);
	
	float distance = length(toLight);
	float att = noAtt ? 1.0 : max(uni_materialAtt[i] / (distance * distance), 0.0);
	vec3 radiance = uni_materialCol[i] * att * shadowFactor;
	
	float NdotL = (noCosTheta || uni_materialNoCosTheta > 0.5) ? 1.0 : max(dot(unorm, uAng), 0.0);
	if(length(uni_materialDir[i]) > 0.0 && !sunlight) {
		if(spotlight) {
			float outerCutoff = cos(radians(abs(uni_materialArgs[i].y)));
			float innerCutoff = cos(radians(abs(uni_materialArgs[i].y)) * (1 - clamp(uni_materialArgs[i].z, 0.0, 1.0)));
			float theta = dot(uAng, normalize(-uni_materialDir[i]));
			float epsilon = innerCutoff - outerCutoff;
			float spotIntensity = clamp((theta - outerCutoff) / epsilon, 0.0, 1.0);
			
			NdotL *= spotIntensity;
		}
		else NdotL *= pow(max(dot(-uAng, normalize(uni_materialDir[i])), 0.0), !arealight ? uni_materialArgs[i].y : 1.0);
	}
	att_materialInfluenceMul[i] = radiance * NdotL;
}

#interpolatedRDF_F

const float PI = 3.14159265359;

float ao = uni_materialAO * glb_materialAO;
vec3 influence = vec3(0.0);
for(int i = 0 ; i < uni_materialLightMax ; ++i) {
	vec3 kDiffuse = vec3(1.0);
	influence += (kDiffuse * out_Color.rgb / PI) * att_materialInfluenceMul[i];
}

vec3 ambient = vec3(uni_materialAmbient) * out_Color.rgb * ao;
vec3 color = ambient + influence;
color = color / (color + vec3(1.0));
color = pow(color, vec3(1.0 / uni_materialGamma));

out_Color = vec4(color, out_Color.a);

