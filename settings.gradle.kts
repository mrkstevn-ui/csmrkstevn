rootProject.name = "CloudstreamPlugins"

// Otomatik tarama yerine eklentini buraya manuel ekliyoruz.
// Eğer klasör adın hala "ExampleProvider" ise burayı öyle bırak,
// ama klasör adını değiştirdiysen o ismi yaz.
include(":ExampleProvider")

// Gelecekte yeni eklentiler eklersen buraya include(":YeniEklenti") diye eklersin.