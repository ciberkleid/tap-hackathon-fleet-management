SOURCE_IMAGE = os.getenv("SOURCE_IMAGE", default='harbor.h2o-2-1249.h2o.vmware.com/tap/supply-chain/tilt-wehaul-fleet-management-app-source')
LOCAL_PATH = os.getenv("LOCAL_PATH", default='.')
NAMESPACE = os.getenv("NAMESPACE", default='team-a')
OUTPUT_TO_NULL_COMMAND = os.getenv("OUTPUT_TO_NULL_COMMAND", default=' > /dev/null ')
os.putenv("KUBECONFIG", "/Users/ciberkleid/.secrets/kubeconfig-team-a")

k8s_custom_deploy(
    'tilt-wehaul-fleet-management-app',
    apply_cmd="tanzu apps workload apply -f config/workload.yaml --debug --live-update" +
              " --local-path " + LOCAL_PATH +
              " --source-image " + SOURCE_IMAGE +
              " --namespace " + NAMESPACE +
              " --yes " +
              OUTPUT_TO_NULL_COMMAND +
              " && kubectl get workload tilt-wehaul-fleet-management-app --namespace " + NAMESPACE + " -o yaml",
    delete_cmd="tanzu apps workload delete -f config/workload.yaml --namespace " + NAMESPACE + " --yes",
    deps=['pom.xml', './target/classes'],
    container_selector='workload',
    live_update=[
      sync('./target/classes', '/workspace/BOOT-INF/classes')
    ]
)

k8s_resource('tilt-wehaul-fleet-management-app', port_forwards=["8080:8080"],
            extra_pod_selectors=[{'serving.knative.dev/service': 'tilt-wehaul-fleet-management-app'}])
