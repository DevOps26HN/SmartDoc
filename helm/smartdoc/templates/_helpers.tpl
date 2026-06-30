{{/*
Expand the name of the chart.
*/}}
{{- define "smartdoc.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Fully qualified app name (release-scoped, used as the prefix for every object).
*/}}
{{- define "smartdoc.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Chart name and version, as used by the helm.sh/chart label.
*/}}
{{- define "smartdoc.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Per-component fully qualified names — give each component a stable DNS name.
*/}}
{{- define "smartdoc.db.fullname" -}}
{{- printf "%s-db" (include "smartdoc.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "smartdoc.server.fullname" -}}
{{- printf "%s-server" (include "smartdoc.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "smartdoc.client.fullname" -}}
{{- printf "%s-client" (include "smartdoc.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "smartdoc.genai.fullname" -}}
{{- printf "%s-genai" (include "smartdoc.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "smartdoc.prometheus.fullname" -}}
{{- printf "%s-prometheus" (include "smartdoc.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "smartdoc.grafana.fullname" -}}
{{- printf "%s-grafana" (include "smartdoc.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Name of the Secret holding the database password (existing or chart-managed).
*/}}
{{- define "smartdoc.db.secretName" -}}
{{- if .Values.db.existingSecret -}}
{{- .Values.db.existingSecret -}}
{{- else -}}
{{- include "smartdoc.db.fullname" . -}}
{{- end -}}
{{- end -}}

{{/*
Name of the Secret holding the GenAI cloud API key (existing or chart-managed).
*/}}
{{- define "smartdoc.genai.secretName" -}}
{{- if .Values.genai.existingSecret -}}
{{- .Values.genai.existingSecret -}}
{{- else -}}
{{- include "smartdoc.genai.fullname" . -}}
{{- end -}}
{{- end -}}

{{/*
Name of the Secret holding Grafana admin credentials (existing or chart-managed).
*/}}
{{- define "smartdoc.grafana.secretName" -}}
{{- if .Values.grafana.existingSecret -}}
{{- .Values.grafana.existingSecret -}}
{{- else -}}
{{- include "smartdoc.grafana.fullname" . -}}
{{- end -}}
{{- end -}}

{{/*
Common labels applied to every object.
*/}}
{{- define "smartdoc.labels" -}}
helm.sh/chart: {{ include "smartdoc.chart" . }}
app.kubernetes.io/name: {{ include "smartdoc.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- with .Values.commonLabels }}
{{ toYaml . }}
{{- end }}
{{- end -}}

{{/*
Selector labels for a given component. Call with a dict:
  (dict "context" . "component" "server")
*/}}
{{- define "smartdoc.selectorLabels" -}}
app.kubernetes.io/name: {{ include "smartdoc.name" .context }}
app.kubernetes.io/instance: {{ .context.Release.Name }}
app.kubernetes.io/component: {{ .component }}
{{- end -}}

{{/*
Render imagePullSecrets if any are configured.
*/}}
{{- define "smartdoc.imagePullSecrets" -}}
{{- with .Values.imagePullSecrets }}
imagePullSecrets:
{{- toYaml . | nindent 0 }}
{{- end }}
{{- end -}}
