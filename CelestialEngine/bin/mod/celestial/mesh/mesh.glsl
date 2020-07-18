
#mesh2DV

gl_Position = uni_transform * vec4(in_position, 1.0);

#meshV

vec4 worldPosition = WORLD_POSITION;
att_clipSpacePosition = uni_projMatrix * uni_viewMatrix * worldPosition;
gl_Position = att_clipSpacePosition;
for(int i = 0 ; i < 8 ; ++i) gl_ClipDistance[i] = dot(worldPosition, uni_clipPlanes[i]);

#meshV_glb

\#define WORLD_POSITION ( (uni_transform * vec4(in_position, 1.0)) + vec4(glb_vertDistort, 0.0) )

#meshF

if(uni_discardEnabled == 1.0 && out_Color.a == 0) discard;
else if(uni_discardEnabled > 0.5 && out_Color.a < 0.1) discard;
