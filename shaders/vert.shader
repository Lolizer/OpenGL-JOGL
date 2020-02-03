#version 430
layout (location=0) in vec3 pos;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 norm;

out vec2 tc; // texture coordinate output to rasterizer for interpolation
out vec4 varyingColor;

struct PositionalLight
{
  vec4 ambient;
  vec4 diffuse;
  vec4 specular;
  vec3 position;
};
struct Material
{
  vec4 ambient;
  vec4 diffuse;
  vec4 specular;
  float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;

layout (binding=1) uniform sampler2D samp; // not used in vertex shader

void main(void)
{
  // convert vertex position to view space
  vec4 P = mv_matrix * vec4(pos,1.0);
  // convert normal to view space
  vec3 N = normalize((norm_matrix * vec4(norm,1.0)).xyz);
  // calculate view space light vector (from vertex to light)
  vec3 L = normalize(light.position - P.xyz);
  // view vector is equivalent to the negative of view space vertex position
  vec3 V = normalize(-P.xyz);
  // R is reflection of -L with respect to surface normal N
  vec3 R = reflect(-L,N);
  // ambient, diffuse, and specular contributions
  vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient *
  material.ambient)).xyz;
  vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(dot(N,L),
  0.0);
  vec3 specular =
  material.specular.xyz * light.specular.xyz * pow(max(dot(R,V), 0.0f),
  material.shininess);
  // send the color output to the fragment shader
  varyingColor = vec4((ambient + diffuse + specular), 1.0);
  // send the position to the fragment shader, as before
  gl_Position = proj_matrix * mv_matrix * vec4(pos,1.0);
  tc = texCoord;
}